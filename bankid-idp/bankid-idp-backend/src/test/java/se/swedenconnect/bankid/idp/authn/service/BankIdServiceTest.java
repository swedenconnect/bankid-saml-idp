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
    // First request should trigger a authenticate and then collect
    BankIDClient client = Mockito.mock(BankIDClient.class);
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    CircuitBreaker circuitBreaker = Mockito.mock(CircuitBreaker.class);
    when(circuitBreaker.tryAcquirePermission()).thenReturn(true);
    OrderResponse data1 = new OrderResponse();
    data1.setQrStartToken("qrs-1");
    data1.setOrderReference("or-1");
    data1.setAutoStartToken("ast-1");
    data1.setQrStartSecret("qss-1");
    OrderResponse data2 = new OrderResponse();
    data1.setQrStartToken("qrs-2");
    data1.setOrderReference("or-2");
    data1.setAutoStartToken("ast-2");
    data1.setQrStartSecret("qss-2");
    when(client.authenticate(any())).thenReturn(Mono.just(data1)).thenReturn(Mono.just(data2));

    when(client.collect(any())).thenAnswer(a -> {
      CollectResponse collectResponse = new CollectResponse();
      collectResponse.setOrderReference(data1.getOrderReference());
      collectResponse.setHintCode("hint");
      collectResponse.setStatus(CollectResponse.Status.PENDING);
      return Mono.just(collectResponse);
    });
    BankIdService service = new BankIdService(new BankIdEventPublisher(publisher), circuitBreaker, new BankIdRequestFactory());
    BankIdContext context = new BankIdContext();
    context.setOperation(BankIdOperation.AUTH);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn("1.1.1.1");
    PollRequest pollRequest1 = PollRequest.builder().qr(false).request(request).context(context).relyingPartyData(new RelyingPartyData(client, List.of("asd"), new DisplayText(), new DisplayText(), new EntityRequirement())).build();
    ApiResponse response1 = service.poll(pollRequest1).block();
    Assertions.assertNotNull(response1);
    Assertions.assertEquals(response1.getAutoStartToken(), data1.getAutoStartToken());
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(1)).collect(any());
    BankIdSessionState sessionState = new BankIdSessionState();
    sessionState.push(BankIdSessionData.of(pollRequest1, data1));
    // Second request should trigger collect
    ApiResponse response2 = service.poll(PollRequest.builder().state(sessionState).qr(false).request(request).context(context).relyingPartyData(new RelyingPartyData(client, List.of("asd"), new DisplayText(), new DisplayText(), new EntityRequirement())).build()).block();
    Mockito.verify(client, times(1)).authenticate(any());
    Mockito.verify(client, times(2)).collect(any());
    Assertions.assertNotNull(response2);
    Assertions.assertEquals(response2.getAutoStartToken(), data1.getAutoStartToken());
    CollectResponse collectResponse = new CollectResponse();
    collectResponse.setStatus(CollectResponse.Status.FAILED);
    collectResponse.setOrderReference("2");
    collectResponse.setHintCode(ErrorCode.START_FAILED.getValue());
    Mockito.when(client.collect(any())).thenAnswer(a -> {
      return Mono.just(collectResponse);
    });
    BankIdSessionData newState = BankIdSessionData.of(sessionState.getBankIdSessionData(), collectResponse);
    sessionState = new BankIdSessionState();
    sessionState.push(newState);
    // Third request should trigger authenticate and collect again with a new autostartToken
    ApiResponse response3 = service.poll(PollRequest.builder().qr(false).request(request).state(sessionState).context(context).relyingPartyData(new RelyingPartyData(client, List.of("asd"), new DisplayText(), new DisplayText(), new EntityRequirement())).build()).block();
    Mockito.verify(client, times(2)).authenticate(any());
    Mockito.verify(client, times(3)).collect(any());
    Assertions.assertNotNull(response3);
    Assertions.assertEquals(response3.getAutoStartToken(), data2.getAutoStartToken());
  }
}