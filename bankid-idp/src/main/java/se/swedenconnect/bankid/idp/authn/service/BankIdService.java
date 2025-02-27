/*
 * Copyright 2023-2025 Sweden Connect
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

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.api.ApiResponseFactory;
import se.swedenconnect.bankid.idp.authn.api.ServiceInformation;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.error.BankIdSessionExpiredException;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

/**
 * The BankID service. This component is responsible of communicating with the BankID server using the RP API.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdService {

  /** The BankID event publisher. */
  private final BankIdEventPublisher eventPublisher;

  /** The circuit breaker (for resilliance). */
  private final CircuitBreaker circuitBreaker;

  /** For generating requests to the BankID server. */
  private final BankIdRequestFactory requestFactory;

  /** Duration to allow retry session start */
  private final Duration bankIdStartRetryDuration;

  /**
   * Constructor.
   *
   * @param eventPublisher the BankID event publisher
   * @param circuitBreaker the circuit breaker (for resilliance)
   * @param requestFactory for generating requests to the BankID server
   * @param bankIdStartRetryDuration duration to allow retry session start
   */
  public BankIdService(final BankIdEventPublisher eventPublisher, final CircuitBreaker circuitBreaker,
      final BankIdRequestFactory requestFactory, Duration bankIdStartRetryDuration) {
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    this.circuitBreaker = Objects.requireNonNull(circuitBreaker, "circuitBreaker must not be null");
    this.requestFactory = Optional.ofNullable(requestFactory).orElseGet(BankIdRequestFactory::new);
    this.bankIdStartRetryDuration = Objects.requireNonNull(bankIdStartRetryDuration);
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
        .map(sessionData -> this.collect(request)
            .map(c -> BankIdSessionData.of(sessionData, c, request.getQr()))
            .flatMap(b -> this.reInitIfExpired(request, b))
            .map(b -> ApiResponseFactory.create(b, request.getRelyingPartyData().getClient().getQRGenerator(),
                request.getQr()))
            .onErrorResume(e -> this.handleError(e, request)))
        .orElseGet(() -> this.onNoSession(request));
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
   * @return an {@link OrderResponse}
   */
  private Mono<OrderResponse> init(final PollRequest request) {
    if (request.getContext().getOperation().equals(BankIdOperation.SIGN)) {
      return request.getRelyingPartyData().getClient()
          .sign(this.requestFactory.createSignRequest(request))
          .transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker))
          .map(o -> {
            this.eventPublisher.orderResponse(request, o).publish();
            return o;
          });
    }
    else {
      return request.getRelyingPartyData().getClient()
          .authenticate(this.requestFactory.createAuthenticateRequest(request))
          .transformDeferred(CircuitBreakerOperator.of(this.circuitBreaker))
          .map(o -> {
            this.eventPublisher.orderResponse(request, o).publish();
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
    return this.init(pollRequest)
        .map(orderResponse -> BankIdSessionData.of(pollRequest, orderResponse))
        .flatMap(sessionData -> pollRequest.getRelyingPartyData().getClient().collect(sessionData.getOrderReference())
            .map(collectResponse -> {
              eventPublisher.collectResponse(pollRequest, collectResponse).publish();
              return ApiResponseFactory.create(BankIdSessionData.of(sessionData, collectResponse, pollRequest.getQr()),
                  pollRequest.getRelyingPartyData().getClient().getQRGenerator(), pollRequest.getQr());
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
  private Mono<BankIdSessionData> reInitIfExpired(final PollRequest request, final BankIdSessionData bankIdSessionData) {
    if (bankIdSessionData.getStartFailed()) {
      Instant initialOrderTime = request.getState().getInitialOrderTime();
      Instant now = Instant.now();
      if (initialOrderTime.isBefore(now) && initialOrderTime.plus(bankIdStartRetryDuration).isBefore(now)) {
        return Mono.error(new BankIdSessionExpiredException(request));
      }
      return this.init(request)
          .map(orderResponse -> BankIdSessionData.of(request, orderResponse))
          .flatMap(updatedSessionData -> request.getRelyingPartyData().getClient().collect(updatedSessionData.getOrderReference())
              .map(collectResponse -> {
                eventPublisher.collectResponse(request, collectResponse).publish();
                return BankIdSessionData.of(updatedSessionData, collectResponse, request.getQr());
              }));
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

  /**
   * Delivers service information.
   *
   * @return a {@link ServiceInformation}
   */
  public Mono<ServiceInformation> getServiceInformation() {
    if (this.circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED)) {
      return Mono.just(new ServiceInformation(ServiceInformation.Status.OK));
    }
    else {
      return Mono.just(new ServiceInformation(ServiceInformation.Status.ISSUES));
    }
  }
}
