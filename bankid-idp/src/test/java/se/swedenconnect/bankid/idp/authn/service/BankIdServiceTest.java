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
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.ResilienceConfiguration;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.impl.BankIdServerException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class BankIdServiceTest {
  @Test
  void pollAndReInit() {
    final ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    final CircuitBreaker circuitBreaker = Mockito.mock(CircuitBreaker.class);
    when(circuitBreaker.tryAcquirePermission()).thenReturn(true);
    final BankIdService service = new BankIdService(new BankIdEventPublisher(publisher), circuitBreaker,
        new BankIdRequestFactory(), Duration.ofMinutes(3), "https://www.example.se/return");
    final BankIDClient client = Mockito.mock(BankIDClient.class);
    final OrderResponse expectedFirstSession = BankIdResponseFixture.createOrderResponse(1);
    final OrderResponse expectedSecondSession = BankIdResponseFixture.createOrderResponse(2);
    final CollectResponse initialCollect = BankIdResponseFixture.createInitial(expectedFirstSession);

    when(client.authenticate(any())).thenReturn(Mono.just(expectedFirstSession))
        .thenReturn(Mono.just(expectedSecondSession));
    when(client.collect(any())).thenAnswer(a -> Mono.just(initialCollect));

    // First request should trigger an authenticate and then collect
    // ---
    final PollRequest pollRequest1 = BankIdResponseFixture.createPollRequest(client);
    final ApiResponse response1 = service.poll(pollRequest1).block();
    Assertions.assertNotNull(response1);
    Assertions.assertEquals(response1.getAutoStartToken(), expectedFirstSession.getAutoStartToken());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(1)).collect(any());
    final BankIdSessionState sessionState = BankIdResponseFixture.create(pollRequest1, expectedFirstSession, null);

    // Second request should trigger collect
    // ---
    final ApiResponse response2 = service.poll(BankIdResponseFixture.createPollrequest(client, sessionState)).block();
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(2)).collect(any());
    Assertions.assertNotNull(response2);
    Assertions.assertEquals(response2.getAutoStartToken(), expectedFirstSession.getAutoStartToken());
    final CollectResponse collectResponse = BankIdResponseFixture.createStartFailed(initialCollect);
    Mockito.when(client.collect(any())).thenAnswer(a -> Mono.just(collectResponse))
        .thenAnswer(a -> Mono.just(BankIdResponseFixture.createInitial(expectedSecondSession)));
    BankIdResponseFixture.update(sessionState, collectResponse);

    // Third request should trigger authenticate and collect again with a new autostartToken
    // ---
    final ApiResponse response3 = service.poll(BankIdResponseFixture.createPollrequest(client, sessionState)).block();
    Mockito.verify(client, times(2)).authenticate(any());
    Mockito.verify(client, times(4)).collect(any());
    Assertions.assertNotNull(response3);
    Assertions.assertEquals(response3.getAutoStartToken(), expectedSecondSession.getAutoStartToken());
  }

  @Test
  void pollToExpiration() {
    final ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    final CircuitBreaker circuitBreaker = Mockito.mock(CircuitBreaker.class);
    when(circuitBreaker.tryAcquirePermission()).thenReturn(true);
    final BankIdService service = new BankIdService(new BankIdEventPublisher(publisher), circuitBreaker,
        new BankIdRequestFactory(), Duration.ofMinutes(3), "https://www.example.se/return");
    final BankIDClient client = Mockito.mock(BankIDClient.class);
    final OrderResponse expectedFirstSession = BankIdResponseFixture.createOrderResponse(1);
    final OrderResponse expectedSecondSession = BankIdResponseFixture.createOrderResponse(2);
    final CollectResponse initialCollect = BankIdResponseFixture.createInitial(expectedFirstSession);

    when(client.authenticate(any())).thenReturn(Mono.just(expectedFirstSession))
        .thenReturn(Mono.just(expectedSecondSession));
    when(client.collect(any())).thenAnswer(a -> Mono.just(initialCollect));

    // First request should trigger an authenticate and then collect
    // ---
    final PollRequest pollRequest1 = BankIdResponseFixture.createPollRequest(client);
    final ApiResponse response1 = service.poll(pollRequest1).block();
    Assertions.assertNotNull(response1);
    Assertions.assertEquals(response1.getAutoStartToken(), expectedFirstSession.getAutoStartToken());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(1)).collect(any());
    final BankIdSessionState sessionState = BankIdResponseFixture.create(pollRequest1, expectedFirstSession, null);

    // Second request should return a timeout message
    // ---
    final PollRequest pollrequest2 = BankIdResponseFixture.createPollrequest(client, sessionState);
    when(client.collect(any())).thenAnswer(a -> {
      final CollectResponse transactionExpired = BankIdResponseFixture.createTransactionExpired(initialCollect);
      return Mono.just(transactionExpired);
    });
    final ApiResponse response2 = service.poll(pollrequest2).block();
    Assertions.assertEquals("bankid.msg.rfa8", response2.getMessageCode());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(2)).collect(any());
  }

  @Test
  void circuitBreakerWillNeverBeOpen() {
    final ResilienceConfiguration resilienceConfiguration = new ResilienceConfiguration();
    final CircuitBreakerConfig config = resilienceConfiguration.circuitBreakerConfig();
    final CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
    final CircuitBreaker circuitBreaker = resilienceConfiguration.circuitBreaker(registry);
    final BankIdService service =
        new BankIdService(new BankIdEventPublisher(Mockito.mock(ApplicationEventPublisher.class)),
            circuitBreaker, new BankIdRequestFactory(), Duration.ofMinutes(3), "https://www.example.se/return");

    final BankIDClient client = Mockito.mock(BankIDClient.class);
    when(client.collect(any())).thenReturn(Mono.error(new BankIdServerException("")));
    when(client.authenticate(any())).thenReturn(Mono.error(new BankIdServerException("")));
    for (int x = 0; x < config.getMinimumNumberOfCalls(); x++) {
      Assertions.assertThrows(BankIdServerException.class,
          () -> service.poll(BankIdResponseFixture.createPollRequest(client)).block());
    }
    try {
      Thread.sleep(50);
    }
    catch (final Exception e) {
      // Do nothing
    }
    Assertions.assertSame(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
  }
}
