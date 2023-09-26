package se.swedenconnect.bankid.idp.authn.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.ResilienceConfiguration;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.impl.BankIdServerException;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;


class BankIdServiceTest {
  @Test
  void pollAndReInit() {
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    CircuitBreaker circuitBreaker = Mockito.mock(CircuitBreaker.class);
    when(circuitBreaker.tryAcquirePermission()).thenReturn(true);
    BankIdService service = new BankIdService(new BankIdEventPublisher(publisher), circuitBreaker, new BankIdRequestFactory());
    BankIDClient client = Mockito.mock(BankIDClient.class);
    OrderResponse expectedFirstSession = BankIdResponseFixture.createOrderResponse(1);
    OrderResponse expectedSecondSession = BankIdResponseFixture.createOrderResponse(2);
    CollectResponse initialCollect = BankIdResponseFixture.createInitial(expectedFirstSession);

    when(client.authenticate(any())).thenReturn(Mono.just(expectedFirstSession)).thenReturn(Mono.just(expectedSecondSession));
    when(client.collect(any())).thenAnswer(a -> {
      return Mono.just(initialCollect);
    });

    // First request should trigger a authenticate and then collect
    // ---
    PollRequest pollRequest1 = BankIdResponseFixture.createPollRequest(client);
    ApiResponse response1 = service.poll(pollRequest1).block();
    Assertions.assertNotNull(response1);
    Assertions.assertEquals(response1.getAutoStartToken(), expectedFirstSession.getAutoStartToken());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(1)).collect(any());
    BankIdSessionState sessionState = BankIdResponseFixture.create(pollRequest1, expectedFirstSession);

    // Second request should trigger collect
    // ---
    ApiResponse response2 = service.poll(BankIdResponseFixture.createPollrequest(client, sessionState)).block();
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(2)).collect(any());
    Assertions.assertNotNull(response2);
    Assertions.assertEquals(response2.getAutoStartToken(), expectedFirstSession.getAutoStartToken());
    CollectResponse collectResponse = BankIdResponseFixture.createStartFailed(initialCollect);
    Mockito.when(client.collect(any())).thenAnswer(a -> {
      return Mono.just(collectResponse);
    }).thenAnswer(a -> {
      return Mono.just(BankIdResponseFixture.createInitial(expectedSecondSession));
    });
    BankIdResponseFixture.update(sessionState, collectResponse);

    // Third request should trigger authenticate and collect again with a new autostartToken
    // ---
    ApiResponse response3 = service.poll(BankIdResponseFixture.createPollrequest(client, sessionState)).block();
    Mockito.verify(client, times(2)).authenticate(any());
    Mockito.verify(client, times(4)).collect(any());
    Assertions.assertNotNull(response3);
    Assertions.assertEquals(response3.getAutoStartToken(), expectedSecondSession.getAutoStartToken());
  }

  @Test
  void pollToExpiration() {
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    CircuitBreaker circuitBreaker = Mockito.mock(CircuitBreaker.class);
    when(circuitBreaker.tryAcquirePermission()).thenReturn(true);
    BankIdService service = new BankIdService(new BankIdEventPublisher(publisher), circuitBreaker, new BankIdRequestFactory());
    BankIDClient client = Mockito.mock(BankIDClient.class);
    OrderResponse expectedFirstSession = BankIdResponseFixture.createOrderResponse(1);
    OrderResponse expectedSecondSession = BankIdResponseFixture.createOrderResponse(2);
    CollectResponse initialCollect = BankIdResponseFixture.createInitial(expectedFirstSession);

    when(client.authenticate(any())).thenReturn(Mono.just(expectedFirstSession)).thenReturn(Mono.just(expectedSecondSession));
    when(client.collect(any())).thenAnswer(a -> {
      return Mono.just(initialCollect);
    });

    // First request should trigger a authenticate and then collect
    // ---
    PollRequest pollRequest1 = BankIdResponseFixture.createPollRequest(client);
    ApiResponse response1 = service.poll(pollRequest1).block();
    Assertions.assertNotNull(response1);
    Assertions.assertEquals(response1.getAutoStartToken(), expectedFirstSession.getAutoStartToken());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(1)).collect(any());
    BankIdSessionState sessionState = BankIdResponseFixture.create(pollRequest1, expectedFirstSession);

    // Second request should return a timeout message
    // ---
    PollRequest pollrequest2 = BankIdResponseFixture.createPollrequest(client, sessionState);
    when(client.collect(any())).thenAnswer(a -> {
      CollectResponse transactionExpired = BankIdResponseFixture.createTransactionExpired(initialCollect);
      return Mono.just(transactionExpired);
    });
    ApiResponse response2 = service.poll(pollrequest2).block();
    Assertions.assertEquals("bankid.msg.rfa8", response2.getMessageCode());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(2)).collect(any());
  }

  @Test
  void circuitBreakerWillNeverBeOpen() {
    ResilienceConfiguration resilienceConfiguration = new ResilienceConfiguration();
    CircuitBreakerConfig config = resilienceConfiguration.circuitBreakerConfig();
    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
    CircuitBreaker circuitBreaker = resilienceConfiguration.circuitBreaker(registry);
    BankIdService service = new BankIdService(new BankIdEventPublisher(Mockito.mock(ApplicationEventPublisher.class)), circuitBreaker, new BankIdRequestFactory());

    BankIDClient client = Mockito.mock(BankIDClient.class);
    when(client.collect(any())).thenReturn(Mono.error(new BankIdServerException("")));
    when(client.authenticate(any())).thenReturn(Mono.error(new BankIdServerException("")));
    for (int x = 0; x < config.getMinimumNumberOfCalls(); x++) {
        Assertions.assertThrows(BankIdServerException.class, () -> service.poll(BankIdResponseFixture.createPollRequest(client)).block());
    }
    try {
      Thread.sleep(50);
    } catch (Exception e) {
      // Do nothing
    }
    Assertions.assertSame(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
  }
}