/*
 * Copyright 2023 Sweden Connect
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
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.ApiResponseFactory;
import se.swedenconnect.bankid.idp.authn.ApiResponse;
import se.swedenconnect.bankid.idp.authn.BankIdSessionExpiredException;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.*;

@Service
@AllArgsConstructor
public class BankIdService {

  private final BankIdEventPublisher eventPublisher;

  public Mono<ApiResponse> poll(PollRequest request) {
    return Optional.ofNullable(request.getState())
        .map(BankIdSessionState::getBankIdSessionData)
        .map(sessionData -> this.collect(request)
            .map(c -> BankIdSessionData.of(sessionData, c))
            .flatMap(b -> this.reAuthIfExpired(request))
            .map(b -> ApiResponseFactory.create(b, request.getRelyingPartyData().getClient().getQRGenerator(), request.getQr()))
            .onErrorResume(e -> handleError(e, request)))
        .orElseGet(() -> this.onNoSession(request));
  }

  public Mono<Void> cancel(final HttpServletRequest request, final BankIdSessionState state,
      final BankIDClient client) {
    this.eventPublisher.orderCancellation(request).publish();
    return client.cancel(state.getBankIdSessionData().getOrderReference());
  }

  private Mono<OrderResponse> auth(PollRequest request) {
    return request.getRelyingPartyData().getClient().authenticate(BankIdRequestFactory.createAuthenticateRequest(request))
        .map(o -> {
          this.eventPublisher.orderResponse(request, o).publish();
          return o;
        });
  }

  private Mono<OrderResponse> init(PollRequest request) {
    if (request.getContext().getOperation().equals(BankIdOperation.SIGN)) {
      return request.getRelyingPartyData().getClient()
          .sign(BankIdRequestFactory.createSignRequest(request))
          .map(o -> {
            this.eventPublisher.orderResponse(request, o).publish();
            return o;
          });
    }
    return this.auth(request);
  }

  private Mono<ApiResponse> onNoSession(final PollRequest pollRequest) {
    return this.init(pollRequest)
        .map(b -> BankIdSessionData.of(pollRequest, b))
        .flatMap(b -> pollRequest.getRelyingPartyData().getClient().collect(b.getOrderReference())
            .map(c -> ApiResponseFactory.create(BankIdSessionData.of(b, c), pollRequest.getRelyingPartyData().getClient().getQRGenerator(), pollRequest.getQr())));
  }

  private Mono<ApiResponse> handleError(final Throwable e, PollRequest request) {
    if (e instanceof final BankIdSessionExpiredException bankIdSessionExpiredException) {
      return this.sessionExpired(bankIdSessionExpiredException.getRequest().getRequest());
    }
    if (e.getCause() instanceof final BankIDException bankIDException && ErrorCode.USER_CANCEL.equals(bankIDException.getErrorCode())) {
      eventPublisher.orderCancellation(request.getRequest()).publish();
      return Mono.just(ApiResponseFactory.createUserCancelResponse());
    }
    return Mono.error(e);
  }

  private Mono<BankIdSessionData> reAuthIfExpired(final PollRequest request) {
    final BankIdSessionState state = request.getState();
    final BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    if (bankIdSessionData.getExpired()) {
      if (Duration.between(state.getInitialOrderTime(), Instant.now()).toMinutes() >= 3) {
        return Mono.error(new BankIdSessionExpiredException(request));
      }
      return this.auth(request)
          .map(orderResponse -> BankIdSessionData.of(request, orderResponse));
    }
    return Mono.just(bankIdSessionData);
  }

  private Mono<CollectResponse> collect(final PollRequest request) {
    return request.getRelyingPartyData().getClient().collect(request.getState().getBankIdSessionData().getOrderReference())
        .map(c -> {
          this.eventPublisher.collectResponse(request, c).publish();
          return c;
        });
  }

  private Mono<ApiResponse> sessionExpired(final HttpServletRequest request) {
    this.eventPublisher.orderCancellation(request).publish();
    return Mono.just(ApiResponseFactory.createErrorResponseTimeExpired());
  }
}
