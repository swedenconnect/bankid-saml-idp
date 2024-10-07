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
package se.swedenconnect.bankid.idp.integration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.web.client.HttpServerErrorException;
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
import se.swedenconnect.bankid.rpapi.types.ProgressStatus;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

import java.util.List;

public class BankIdIdpIT extends BankIdIdpIntegrationSetup {

  @Autowired
  private BankIdApiController controller;

  @Test
  void emptyRequestContext_WillFail() {
    final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(servletRequest.getSession()).thenReturn(Mockito.mock(HttpSession.class));
    Assertions.assertThrows(UnrecoverableSaml2IdpException.class, () -> this.controller.poll(servletRequest, null).block());
  }

  @Test
  @WithSamlUser
  void genericFailureOnPoll(final FrontendClient client) {
    final OrderResponse start = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(start);
    final ApiResponse apiResponse = client.poll(true).block();
    Assertions.assertNotNull(apiResponse);
    Assertions.assertNotNull(apiResponse.getAutoStartToken());
    final CollectResponse collect = BankIdResponseFactory.collect(start, c -> c.status(CollectResponse.Status.FAILED));
    BankIdApiMock.nextCollect(collect);
    final ApiResponse failed = client.poll(true).block();
    Assertions.assertNotNull(failed);
    Assertions.assertEquals(ApiResponse.Status.ERROR, failed.getStatus());
  }

  @Test
  @WithSamlUser
  void userCanCompleteLogin(final FrontendClient client) throws JsonProcessingException {
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    final ApiResponse apiResponse = client.poll(true).block();
    Assertions.assertNotNull(apiResponse);
    Assertions.assertNotNull(apiResponse.getAutoStartToken());
    BankIdApiMock.completeCollect(orderResponse);
    final ApiResponse completed = client.poll(true).block();
    Assertions.assertNotNull(completed);
    Assertions.assertNotNull(completed.getAutoStartToken());
    Assertions.assertEquals(ApiResponse.Status.COMPLETE, completed.getStatus());
    final String completionUrl = client.complete();
    Assertions.assertEquals("https://local.dev.swedenconnect.se:8443/idp/resume", completionUrl);
  }

  @Test
  @WithSamlUser
  void userCanRenewSessionOnStartFailed(final FrontendClient client) {
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    ApiResponse apiResponse = client.poll(true).block();
    Assertions.assertEquals(orderResponse.getAutoStartToken(), apiResponse.getAutoStartToken());
    final OrderResponse next = BankIdResponseFactory.start();
    BankIdApiMock.failStart(orderResponse, next);
    apiResponse = client.poll(true).block();
    Assertions.assertEquals(next.getAutoStartToken(), apiResponse.getAutoStartToken());
  }

  @Test
  @WithSamlUser(isSign = true)
  void userCanSign(final FrontendClient client) throws JsonProcessingException {
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockSign(orderResponse);
    final ApiResponse polled = client.poll(true).block();
    Assertions.assertEquals(orderResponse.getAutoStartToken(), polled.getAutoStartToken());
    BankIdApiMock.completeCollect(orderResponse);
    final ApiResponse completed = client.poll(true).block();
    Assertions.assertEquals("COMPLETE", completed.getStatus().name());
    final String complete = client.complete();
    Assertions.assertEquals("https://local.dev.swedenconnect.se:8443/idp/resume", complete);
  }

  @Test
  @WithSamlUser
  void userCanCancelSession(final FrontendClient client) {
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    BankIdApiMock.mockCancel(orderResponse);
    final ApiResponse polled = client.poll(true).block();
    Assertions.assertEquals(orderResponse.getAutoStartToken(), polled.getAutoStartToken());
    client.cancelApi().block();
    final String cancel = client.cancel();
    Assertions.assertEquals("https://local.dev.swedenconnect.se:8443/idp/resume", cancel);
    BankIdApiMock.nextCollect(BankIdResponseFactory.collect(orderResponse,
        c -> c.hintCode("userCancel").status(CollectResponse.Status.FAILED)));
    final ApiResponse poll = client.poll(true).block();
    Assertions.assertEquals("CANCEL", poll.getStatus().name());
  }

  @Test
  @WithSamlUser
  void userCanNotPollInParallel(final FrontendClient client) {
    BankIdApiMock.setDelay(200);
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    final List<String> dummyList = List.of("NOT USED ON PURPOSE", "NOT USED ON PURPOSE");
    StepVerifier.create(Flux.fromIterable(dummyList).flatMap(dnu -> client.poll(true)))
        .expectErrorMatches(e -> e instanceof CannotAcquireLockException)
        .verify();
    BankIdApiMock.resetDelay();
  }

  @Test
  @WithSamlUser
  void userCanChangeBetweenShowingQrCode(final FrontendClient client) {
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    BankIdApiMock.nextCollect(BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ProgressStatus.OUTSTANDING_TRANSACTION.getValue())));
    Assertions.assertEquals("bankid.msg.rfa13", client.poll(false).block().getMessageCode());
    Assertions.assertEquals("bankid.msg.ext2", client.poll(true).block().getMessageCode());
    Assertions.assertEquals("bankid.msg.rfa13", client.poll(false).block().getMessageCode());
  }

  @Test
  @WithSamlUser
  void previousSelectedDeviceIsUsedDevice(final FrontendClient client) {
    final OrderResponse orderResponse = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(orderResponse);
    BankIdApiMock.nextCollect(BankIdResponseFactory.collect(orderResponse, c -> c.hintCode(ProgressStatus.OUTSTANDING_TRANSACTION.getValue())));
    Assertions.assertEquals("bankid.msg.ext2", client.poll(true).block().getMessageCode());
    Assertions.assertEquals("bankid.msg.rfa13", client.poll(false).block().getMessageCode());
    BankIdApiMock.nextCollect(BankIdResponseFactory.complete(orderResponse));
    final ApiResponse apiResponse = client.poll(false).block();
    Assertions.assertEquals(ApiResponse.Status.COMPLETE, apiResponse.getStatus());
    client.complete();
    Assertions.assertEquals("this", client.device().block().getDevice());
  }


  @ParameterizedTest
  @MethodSource({"se.swedenconnect.bankid.idp.integration.fixtures.MessageValidationArguments#getAll"})
  void testUserMessage(final String expectedMessageCode, final Boolean sign, final OrderAndCollectResponse response, final Boolean showQr) {
    final FrontendClient client = AuthenticatedClientResolver.createFrontEndClient(sign);
    if (sign) {
      BankIdApiMock.mockSign(response.getOrderResponse());
    }
    else {
      BankIdApiMock.mockAuth(response.getOrderResponse());
    }
    BankIdApiMock.nextCollect(response.getCollectResponse());
    StepVerifier.create(client.poll(showQr))
        .expectNextMatches(a -> expectedMessageCode.equals(a.getMessageCode()))
        .verifyComplete();
  }

  @Test
  @WithSamlUser
  void testUserSessionExpiredWillReturn400(final FrontendClient client) {
    final OrderResponse start = BankIdResponseFactory.start();
    BankIdApiMock.mockAuth(start);
    final ApiResponse apiResponse = client.poll(false).block();
    Assertions.assertEquals(start.getAutoStartToken(), apiResponse.getAutoStartToken());
    this.clearSessions();
    Assertions.assertThrows(HttpServerErrorException.class, () -> client.poll(false).block());
  }
}
