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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.ApiResponseFactory;
import se.swedenconnect.bankid.idp.authn.ApiResponse;
import se.swedenconnect.bankid.idp.authn.BankIdSessionExpiredException;
import se.swedenconnect.bankid.idp.authn.ServiceInformation;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BankIdService {

    private final BankIdEventPublisher eventPublisher;

    private final BankIdRequestFactory requestFactory;

    private final CircuitBreaker circuitBreaker;

    public Mono<ApiResponse> poll(final PollRequest request) {
        return Optional.ofNullable(request.getState())
                .map(BankIdSessionState::getBankIdSessionData)
                .map(sessionData -> this.collect(request)
                        .map(c -> BankIdSessionData.of(sessionData, c))
                        .flatMap(b -> this.reAuthIfExpired(request))
                        .map(b -> ApiResponseFactory.create(b, request.getRelyingPartyData().getClient().getQRGenerator(), request.getQr()))
                        .onErrorResume(e -> handleError(e, request)))
                .orElseGet(() -> this.onNoSession(request));
    }

    public Mono<Void> cancel(final HttpServletRequest request, final BankIdSessionState state, final RelyingPartyData data) {
        this.eventPublisher.orderCancellation(request, data).publish();
        return data.getClient().cancel(state.getBankIdSessionData().getOrderReference())
      .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }

    private Mono<OrderResponse> auth(final PollRequest request) {
        return request.getRelyingPartyData().getClient()
                .authenticate(requestFactory.createAuthenticateRequest(request))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .map(o -> {
                    this.eventPublisher.orderResponse(request, o).publish();
                    return o;
                });
    }

    private Mono<OrderResponse> init(final PollRequest request) {
        if (request.getContext().getOperation().equals(BankIdOperation.SIGN)) {
            return request.getRelyingPartyData().getClient()
                    .sign(requestFactory.createSignRequest(request))
                    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
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

    private Mono<ApiResponse> handleError(final Throwable e, final PollRequest request) {
        if (e instanceof final BankIdSessionExpiredException bankIdSessionExpiredException) {
            return this.sessionExpired(bankIdSessionExpiredException.getRequest().getRequest(), request);
        }
        if (e.getCause() instanceof final BankIDException bankIDException && ErrorCode.USER_CANCEL.equals(bankIDException.getErrorCode())) {
            eventPublisher.orderCancellation(request.getRequest(), request.getRelyingPartyData()).publish();
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
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .map(c -> {
                    this.eventPublisher.collectResponse(request, c).publish();
                    return c;
                });
    }

    private Mono<ApiResponse> sessionExpired(final HttpServletRequest request, final PollRequest pollRequest) {
        this.eventPublisher.orderCancellation(request, pollRequest.getRelyingPartyData()).publish();
        return Mono.just(ApiResponseFactory.createErrorResponseTimeExpired());
    }

    public Mono<ServiceInformation> getServiceInformation() {
        if (circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED)) {
            return Mono.just(new ServiceInformation(ServiceInformation.Status.OK));
        }
        return Mono.just(new ServiceInformation(ServiceInformation.Status.ISSUES));
    }
}
