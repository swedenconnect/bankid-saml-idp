package se.swedenconnect.bankid.idp.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import se.swedenconnect.bankid.idp.argument.WithSamlUser;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.api.BankIdApiController;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


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
  void genericFailureOnPoll(BankIdFrontendClient client) {
    String orderReference = BankIdApiMock.mockAuth();
    ApiResponse apiResponse = client.poll();
    Assertions.assertNotNull(apiResponse);
    Assertions.assertNotNull(apiResponse.getAutoStartToken());
    BankIdApiMock.pendingCollect(orderReference, CollectResponse.Status.FAILED);
    ApiResponse failed = client.poll();
    Assertions.assertNotNull(failed);
    Assertions.assertEquals(failed.getStatus(), ApiResponse.Status.ERROR);
  }
}
