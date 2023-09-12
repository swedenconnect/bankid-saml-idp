package se.swedenconnect.bankid.idp.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import se.swedenconnect.bankid.idp.argument.WithSamlUser;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.api.BankIdApiController;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.ErrorCode;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class BankIdIdpIT extends BankIdIdpIntegrationSetup {

  @Autowired
  private BankIdApiController controller;

  @Test
  void emptyRequestContext_WillFail() {
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(servletRequest.getSession()).thenReturn(Mockito.mock(HttpSession.class));
    Assertions.assertThrows(UnrecoverableSaml2IdpException.class, () -> controller.poll(servletRequest, false).block());
  }

  @Test
  @WithSamlUser
  void genericFailureOnPoll(FrontendClient client) {
    OrderResponse start = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(start);
    ApiResponse apiResponse = client.poll(true).block();
    Assertions.assertNotNull(apiResponse);
    Assertions.assertNotNull(apiResponse.getAutoStartToken());
    CollectResponse collect = BankIdResponseFactory.collect(start, c -> c.status(CollectResponse.Status.FAILED));
    BankIdApiMock.nextCollect(collect);
    ApiResponse failed = client.poll(true).block();
    Assertions.assertNotNull(failed);
    Assertions.assertEquals(ApiResponse.Status.ERROR, failed.getStatus());
  }

  @Test
  @WithSamlUser
  void userCanCompleteLogin(FrontendClient client) throws JsonProcessingException {
    OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    ApiResponse apiResponse = client.poll(true).block();
    Assertions.assertNotNull(apiResponse);
    Assertions.assertNotNull(apiResponse.getAutoStartToken());
    BankIdApiMock.completeCollect(orderResponse);
    ApiResponse completed = client.poll(true).block();
    Assertions.assertNotNull(completed);
    Assertions.assertNotNull(completed.getAutoStartToken());
    Assertions.assertEquals(ApiResponse.Status.COMPLETE, completed.getStatus());
    String completionUrl = client.complete();
    Assertions.assertEquals("https://local.dev.swedenconnect.se:8443/idp/resume", completionUrl);
  }

  @Test
  @WithSamlUser
  void userCanRenewSessionOnStartFailed(FrontendClient client) {
    OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    ApiResponse apiResponse = client.poll(true).block();
    Assertions.assertEquals(orderResponse.getAutoStartToken(), apiResponse.getAutoStartToken());
    OrderResponse next = BankIdResponseFactory.start();
    BankIdApiMock.failStart(orderResponse, next);
    apiResponse = client.poll(true).block();
    Assertions.assertEquals(next.getAutoStartToken(), apiResponse.getAutoStartToken());
  }

  @Test
  @WithSamlUser
  void userCanCancelSession(FrontendClient client) {
    OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    client.poll(true);
    String cancel = client.cancel();
    Assertions.assertEquals("https://local.dev.swedenconnect.se:8443/idp/resume", cancel);
    BankIdApiMock.nextCollect(BankIdResponseFactory.collect(orderResponse, c -> c.hintCode("userCancel").status(CollectResponse.Status.FAILED)));
    ApiResponse poll = client.poll(true).block();
    Assertions.assertEquals("ERROR", poll.getStatus().name());
  }

  @Test
  @WithSamlUser
  void userCanNotPollInParallel(FrontendClient client) {
    BankIdApiMock.setDelay(200);
    OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    List<String> dummyList = List.of("NOT USED ON PURPOSE", "NOT USED ON PURPOSE");
    StepVerifier.create(Flux.fromIterable(dummyList).flatMap(dnu -> client.poll(true)))
        .expectErrorMatches(e -> e instanceof CannotAcquireLockException)
        .verify();
    BankIdApiMock.resetDelay();
  }

  @Test
  @WithSamlUser
  void testUserMessage_auth_noQR(FrontendClient client) {
    OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);

    List<CollectResponse> collectResponses = new ArrayList<>();

    collectResponses.addAll(List.of(
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ProgressStatus.NO_CLIENT.getValue())),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ErrorCode.CANCELLED.getValue()).status(CollectResponse.Status.FAILED)),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ErrorCode.ALREADY_IN_PROGRESS.getValue()).status(CollectResponse.Status.FAILED)),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ErrorCode.MAINTENANCE.getValue()).status(CollectResponse.Status.FAILED)),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ErrorCode.USER_CANCEL.getValue()).status(CollectResponse.Status.FAILED)),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ErrorCode.EXPIRED_TRANSACTION.getValue()).status(CollectResponse.Status.FAILED)),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ProgressStatus.USER_SIGN.getValue())),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ProgressStatus.OUTSTANDING_TRANSACTION.getValue())),
        BankIdResponseFactory.collect(orderResponse, c -> c),
        BankIdResponseFactory.collect(orderResponse, c -> c.status(CollectResponse.Status.FAILED)),
        BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ProgressStatus.USER_MRTD.getValue()))
    ));

    Flux<ApiResponse> responses = Flux.fromIterable(collectResponses)
        .delayElements(Duration.ofMillis(500))
        .flatMap(c -> {
          BankIdApiMock.nextCollect(c);
          return client.poll(false);
        });

    StepVerifier.create(responses)
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa1"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa3"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa4"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa5"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa6"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa8"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa9-auth"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa13"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa21-auth"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa22"))
        .expectNextMatches(a -> a.getMessageCode().equals("bankid.msg.rfa23"))
        .verifyComplete();
  }
}
