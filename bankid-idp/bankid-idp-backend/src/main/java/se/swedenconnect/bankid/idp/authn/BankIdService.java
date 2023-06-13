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
package se.swedenconnect.bankid.idp.authn;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.ApiResponseFactory;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.Requirement;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

@Service
@AllArgsConstructor
public class BankIdService {

  private final BankIdEventPublisher eventPublisher;

  public Mono<ApiResponse> poll(final HttpServletRequest request, final Boolean qr, final BankIdSessionState state,
      final Saml2UserAuthenticationInputToken authnInputToken, final BankIdContext bankIdContext,
      final BankIDClient client, final UserVisibleData message) {
    return Optional.ofNullable(state)
        .map(BankIdSessionState::getBankIdSessionData)
        .map(sessionData -> this.collect(request, client, sessionData)
            .map(c -> BankIdSessionData.of(sessionData, c))
            .flatMap(b -> this.reAuthIfExpired(request, state, bankIdContext, client, message))
            .map(b -> ApiResponseFactory.create(b, client.getQRGenerator(), qr))
            .onErrorResume(this::handleError))
        .orElseGet(() -> this.onNoSession(request, qr, bankIdContext, client, message));
  }

  public Mono<Void> cancel(final HttpServletRequest request, final BankIdSessionState state,
      final BankIDClient client) {
    this.eventPublisher.orderCancellation(request).publish();
    return client.cancel(state.getBankIdSessionData().getOrderReference());
  }

  private Mono<OrderResponse> auth(final HttpServletRequest request, final String personalNumber,
      final BankIDClient client, final Boolean showQr, final UserVisibleData message) {
    final Requirement requirement = new Requirement();
    // TODO: 2023-05-17 Requirement factory per entityId
    return client.authenticate(personalNumber, request.getRemoteAddr(), message, requirement)
        .map(o -> {
          this.eventPublisher.orderResponse(request, o, showQr).publish();
          return o;
        });
  }

  private Mono<OrderResponse> init(final BankIdContext bankIdContext, final HttpServletRequest request,
      final BankIDClient client, final boolean qr, final UserVisibleData message) {
    if (bankIdContext.getOperation().equals(BankIdOperation.SIGN)) {
      return client
          .sign(bankIdContext.getPersonalNumber(), request.getRemoteAddr(), (DataToSign) message, new Requirement())
          .map(o -> {
            this.eventPublisher.orderResponse(request, o, qr).publish();
            return o;
          });
    }
    return this.auth(request, bankIdContext.getPersonalNumber(), client, qr, message);
  }

  private Mono<ApiResponse> onNoSession(final HttpServletRequest request, final Boolean qr,
      final BankIdContext bankIdContext, final BankIDClient client, final UserVisibleData message) {
    return this.init(bankIdContext, request, client, qr, message)
        .map(b -> BankIdSessionData.of(b, qr))
        .flatMap(b -> collect(request, client, b)
            .map(c -> ApiResponseFactory.create(BankIdSessionData.of(b, c), client.getQRGenerator(), qr)));
  }

  private Mono<ApiResponse> handleError(final Throwable e) {
    if (e instanceof final BankIdSessionExpiredException bankIdSessionExpiredException) {
      return this.sessionExpired(bankIdSessionExpiredException.getExpiredSessionHolder());
    }
    return Mono.error(e);
  }

  private Mono<BankIdSessionData> reAuthIfExpired(final HttpServletRequest request, final BankIdSessionState state,
      final BankIdContext bankIdContext, final BankIDClient client, final UserVisibleData message) {
    final BankIdSessionData bankIdSessionData = state.getBankIdSessionData();
    if (bankIdSessionData.getExpired()) {
      if (Duration.between(state.getInitialOrderTime(), Instant.now()).toMinutes() >= 3) {
        return Mono.error(new BankIdSessionExpiredException(request));
      }
      return this.auth(request, bankIdContext.getPersonalNumber(), client, bankIdSessionData.getShowQr(), message)
          .map(orderResponse -> BankIdSessionData.of(orderResponse, bankIdSessionData.getShowQr()));
    }
    return Mono.just(bankIdSessionData);
  }

  private Mono<CollectResponse> collect(final HttpServletRequest request, final BankIDClient client,
      final BankIdSessionData data) {
    return client.collect(data.getOrderReference())
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
