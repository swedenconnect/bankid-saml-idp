/*
 * Copyright 2023-2024 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.authn.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.api.ApiResponseFactory;
import se.swedenconnect.bankid.idp.authn.api.ServiceInformation;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.error.BankIdSecurityViolationError;
import se.swedenconnect.bankid.idp.authn.error.BankIdSecurityViolationException;
import se.swedenconnect.bankid.idp.authn.error.BankIdSessionExpiredException;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The BankID service. This component is responsible for communicating with the BankID server using the RP API.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Slf4j
public class BankIdService {

  /** The BankID event publisher. */
  private final BankIdEventPublisher eventPublisher;

  /** The circuit breaker (for resilliance). */
  private final CircuitBreaker circuitBreaker;

  /** For generating requests to the BankID server. */
  private final BankIdRequestFactory requestFactory;

  /** Duration to allow retry session start */
  private final Duration bankIdStartRetryDuration;

  /** The return URL to use when autostarting the app. */
  private final String returnUrl;

  /**
   * Constructor.
   *
   * @param eventPublisher the BankID event publisher
   * @param circuitBreaker the circuit breaker (for resilliance)
   * @param requestFactory for generating requests to the BankID server
   * @param bankIdStartRetryDuration duration to allow retry session start
   * @param returnUrl the return URL to use when autostarting the app
   */
  public BankIdService(final BankIdEventPublisher eventPublisher, final CircuitBreaker circuitBreaker,
      final BankIdRequestFactory requestFactory, final Duration bankIdStartRetryDuration, final String returnUrl) {
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    this.circuitBreaker = Objects.requireNonNull(circuitBreaker, "circuitBreaker must not be null");
    this.requestFactory = Optional.ofNullable(requestFactory).orElseGet(BankIdRequestFactory::new);
    this.bankIdStartRetryDuration = Objects.requireNonNull(bankIdStartRetryDuration);
    this.returnUrl = Objects.requireNonNull(returnUrl, "returnUrl must not be null");
  }

  /**
   * Sends a request to the BankID server. If we don't have a session we initiate an auth or sign operation.
   *
   * @param request the {@link PollRequest}
   * @return an {@link ApiResponse}
   */
  public Mono<ApiResponse> poll(final PollRequest request) {
    return Optional.ofNullable(request.getState())
        .map(BankIdSessionState::getBankIdSessionData)
        .map(s -> BankIdSessionData.updateFromPolling(s, request))
        .map(this::checkMatchingNonce)
        .map(sessionData -> this.collect(request)
            .map(c -> BankIdSessionData.updateFromResponse(sessionData, c))
            .flatMap(b -> this.reInitIfExpired(request, b))
            .map(b -> ApiResponseFactory.create(b, request.getRelyingPartyData().getClient().getQRGenerator(),
                request.isQr()))
            .onErrorResume(e -> this.handleError(e, request)))
        .orElseGet(() -> this.onNoSession(request))
        .onErrorResume(e -> this.handleError(e, request));
  }

  private BankIdSessionData checkMatchingNonce(final BankIdSessionData sessionData)
      throws BankIdSecurityViolationException {
    if (sessionData.getNonce() != null && sessionData.getReceivedNonce() != null) {
      if (!Objects.equals(sessionData.getNonce(), sessionData.getReceivedNonce())) {
        throw new BankIdSecurityViolationException(BankIdSecurityViolationError.NONCE_MISMATCH,
            "Received nonce does not match expected nonce value");
      }
    }
    return sessionData;
  }

  /**
   * Handles a cancelled operation.
   *
   * @param request the HTTP servlet request
   * @param state the BankID session state
   * @param data the RP
   * @return nothing
   */
  public Mono<Void> cancel(
      final HttpServletRequest request, final BankIdSessionState state, final RelyingPartyData data) {

    this.eventPublisher.orderCancellation(request, data).publish();
    return data.getClient()
        .cancel(state.getBankIdSessionData().getOrderReference())
        .transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker));
  }

  /**
   * Initiates an operation.
   *
   * @param request the {@link PollRequest}
   * @param nonce if autostart with return URL is active, this method contains the nonce (otherwise {@code null})
   * @return an {@link OrderResponse}
   */
  @Nonnull
  private Mono<OrderResponse> init(
      @Nonnull final PollRequest request,
      @Nullable final String nonce) {
    final String appReturnUrl = nonce != null ? this.returnUrl : null;

    if (request.getContext().getOperation() == BankIdOperation.SIGN) {
      return request.getRelyingPartyData().getClient()
          .sign(this.requestFactory.createSignRequest(request, appReturnUrl, nonce))
          .transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker))
          .map(o -> {
            this.eventPublisher.orderResponse(request, o, nonce).publish();
            return o;
          });
    }
    else {
      return request.getRelyingPartyData().getClient()
          .authenticate(this.requestFactory.createAuthenticateRequest(request, appReturnUrl, nonce))
          .transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker))
          .map(o -> {
            this.eventPublisher.orderResponse(request, o, nonce).publish();
            return o;
          });
    }
  }

  /**
   * Is invoked if we don't have a BankID session. Will initiate an operation.
   *
   * @param pollRequest the {@link PollRequest}
   * @return an {@link ApiResponse}
   */
  private Mono<ApiResponse> onNoSession(final PollRequest pollRequest) {
    this.eventPublisher.receivedRequest(pollRequest.getRequest(), pollRequest.getRelyingPartyData(), pollRequest)
        .publish();
    final String nonce = pollRequest.isAutoStartWithReturnUrl() ? UUID.randomUUID().toString() : null;
    return this.init(pollRequest, nonce)
        .map(orderResponse -> BankIdSessionData.initialize(pollRequest, orderResponse, nonce))
        .flatMap(sessionData -> pollRequest.getRelyingPartyData().getClient().collect(sessionData.getOrderReference())
            .map(collectResponse -> {
              this.eventPublisher.collectResponse(pollRequest, collectResponse).publish();
              return ApiResponseFactory.create(BankIdSessionData.updateFromResponse(sessionData, collectResponse),
                  pollRequest.getRelyingPartyData().getClient().getQRGenerator(),
                  pollRequest.isQr());
            }));
  }

  /**
   * Handles errors.
   *
   * @param e the error
   * @param request the polling request
   * @return an {@link ApiResponse}
   */
  private Mono<ApiResponse> handleError(final Throwable e, final PollRequest request) {
    if (e instanceof final BankIdSessionExpiredException bankIdSessionExpiredException) {
      this.eventPublisher.bankIdErrorEvent(request.getRequest(), request.getRelyingPartyData(),
              ErrorCode.EXPIRED_TRANSACTION, bankIdSessionExpiredException.getMessage())
          .publish();
      return this.sessionExpired(bankIdSessionExpiredException.getRequest().getRequest(), request);
    }
    if (e instanceof final BankIdSecurityViolationException violationException) {
      this.eventPublisher.bankIdSecurityViolationEvent(request.getRequest(), request.getRelyingPartyData(),
          violationException.getError(), violationException.getMessage()).publish();
      return this.securityViolation(request.getRequest(), request, violationException);
    }
    if (e.getCause() instanceof final BankIDException bankIdException) {
      if (ErrorCode.USER_CANCEL == bankIdException.getErrorCode()) {
        this.eventPublisher.orderCancellation(request.getRequest(), request.getRelyingPartyData()).publish();
        return Mono.just(ApiResponseFactory.createUserCancelResponse());
      }
      else if (ErrorCode.EXPIRED_TRANSACTION == bankIdException.getErrorCode()) {
        this.eventPublisher.bankIdErrorEvent(request.getRequest(), request.getRelyingPartyData(),
            ErrorCode.EXPIRED_TRANSACTION, "BankID response timeout").publish();
        return Mono.just(ApiResponseFactory.createErrorResponseTimeExpired());
      }
      else {
        this.eventPublisher.bankIdErrorEvent(request.getRequest(), request.getRelyingPartyData(),
            bankIdException.getErrorCode(), bankIdException.getDetails()).publish();
        return Mono.error(e);
      }
    }

    this.eventPublisher.bankIdErrorEvent(request.getRequest(), request.getRelyingPartyData(),
        ErrorCode.UNKNOWN_ERROR, e.getMessage()).publish();
    return Mono.error(e);
  }

  /**
   * If the session has expired we initiate a new auth/sign operation.
   *
   * @param request the {@link PollRequest}
   * @return a {@link BankIdSessionData}
   */
  private Mono<BankIdSessionData> reInitIfExpired(
      final PollRequest request, final BankIdSessionData bankIdSessionData) {
    if (bankIdSessionData.getStartFailed()) {
      final Instant initialOrderTime = request.getState().getInitialOrderTime();
      final Instant now = Instant.now();
      if (initialOrderTime.isBefore(now) && initialOrderTime.plus(this.bankIdStartRetryDuration).isBefore(now)) {
        return Mono.error(new BankIdSessionExpiredException(request));
      }

      final String nonce = request.isAutoStartWithReturnUrl() ? UUID.randomUUID().toString() : null;
      return this.init(request, nonce)
          .map(orderResponse -> BankIdSessionData.initialize(request, orderResponse, nonce))
          .flatMap(updatedSessionData -> {
            return request.getRelyingPartyData().getClient().collect(updatedSessionData.getOrderReference())
                .map(collectResponse -> {
                  this.eventPublisher.collectResponse(request, collectResponse).publish();
                  return BankIdSessionData.updateFromResponse(updatedSessionData, collectResponse);
                });
          });
    }
    else {
      return Mono.just(bankIdSessionData);
    }
  }

  /**
   * Collects a response.
   *
   * @param request the {@link PollRequest}
   * @return a {@link CollectResponse}
   */
  private Mono<CollectResponse> collect(final PollRequest request) {
    return request.getRelyingPartyData().getClient()
        .collect(request.getState().getBankIdSessionData().getOrderReference())
        .transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker))
        .map(c -> {
          this.eventPublisher.collectResponse(request, c).publish();
          return c;
        });
  }

  /**
   * Is invoked if the session has expired.
   *
   * @param request the HTTP servlet request
   * @param pollRequest the {@link PollRequest}
   * @return an {@link ApiResponse}
   */
  private Mono<ApiResponse> sessionExpired(final HttpServletRequest request, final PollRequest pollRequest) {
    this.eventPublisher.orderCancellation(request, pollRequest.getRelyingPartyData()).publish();
    return Mono.just(ApiResponseFactory.createErrorResponseTimeExpired());
  }

  private Mono<ApiResponse> securityViolation(final HttpServletRequest request, final PollRequest pollRequest,
      final BankIdSecurityViolationException securityViolationException) {

    // We have detected a security violation, first cancel the operation ...
    //
    try {
      // TODO: This will send an event that removes the session ... and there is no way we can send back a useful response ...
      //
      this.cancel(request, pollRequest.getState(), pollRequest.getRelyingPartyData());
    }
    catch (final Exception e) {
      log.info("Error while cancelling order after security violation", e);
    }
    return Mono.just(ApiResponseFactory.createErrorSecurityViolation());
  }

  /**
   * Delivers service information.
   *
   * @return a {@link ServiceInformation}
   */
  public Mono<ServiceInformation> getServiceInformation() {
    if (this.circuitBreaker.getState() == CircuitBreaker.State.CLOSED) {
      return Mono.just(new ServiceInformation(ServiceInformation.Status.OK));
    }
    else {
      return Mono.just(new ServiceInformation(ServiceInformation.Status.ISSUES));
    }
  }

}
