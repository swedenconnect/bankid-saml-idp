package se.swedenconnect.bankid.idp.authn.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import se.swedenconnect.bankid.idp.authn.DisplayText;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.config.EntityRequirement;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


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
    });
    BankIdResponseFixture.update(sessionState, collectResponse);

    // Third request should trigger authenticate and collect again with a new autostartToken
    // ---
    ApiResponse response3 = service.poll(BankIdResponseFixture.createPollrequest(client, sessionState)).block();
    Mockito.verify(client, times(2)).authenticate(any());
    Mockito.verify(client, times(3)).collect(any());
    Assertions.assertNotNull(response3);
    Assertions.assertEquals(response3.getAutoStartToken(), expectedSecondSession.getAutoStartToken());
  }
}