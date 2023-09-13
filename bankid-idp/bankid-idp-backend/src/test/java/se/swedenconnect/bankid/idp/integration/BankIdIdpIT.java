package se.swedenconnect.bankid.idp.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import se.swedenconnect.bankid.idp.argument.AuthenticatedClientResolver;
import se.swedenconnect.bankid.idp.argument.WithSamlUser;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.api.BankIdApiController;
import se.swedenconnect.bankid.idp.integration.client.FrontendClient;
import se.swedenconnect.bankid.idp.integration.response.OrderAndCollectResponse;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

  @ParameterizedTest
  @MethodSource({"se.swedenconnect.bankid.idp.integration.fixtures.MessageValidationArguments#getAll"})
  void testUserMessage(String expectedMessageCode, Boolean sign, OrderAndCollectResponse response, Boolean showQr) {
    FrontendClient client = AuthenticatedClientResolver.createFrontEndClient(sign);
    if (sign) {
      BankIdApiMock.mockSign(response.getOrderResponse());
    } else {
      BankIdApiMock.mockAuth(response.getOrderResponse());
    }
    BankIdApiMock.nextCollect(response.getCollectResponse());
    StepVerifier.create(client.poll(showQr))
        .expectNextMatches(a -> expectedMessageCode.equals(a.getMessageCode()))
        .verifyComplete();
  }
}
