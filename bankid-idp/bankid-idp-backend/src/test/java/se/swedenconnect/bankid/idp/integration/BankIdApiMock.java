package se.swedenconnect.bankid.idp.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class BankIdApiMock {
  private static final WireMockServer server;

  static {
    server = new WireMockServer(9000);
    server.start();
  }

  public static String mockAuth() {
    String orderRef = UUID.randomUUID().toString();
    String response = """
        {
          "orderRef": "%s",
          "autoStartToken": "%s",
          "qrStartToken": "%s",
          "qrStartSecret": "%s",
          "status": "%s"
        }
        """.formatted(
        orderRef,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        "PENDING"
    );
    server.stubFor(post("/auth").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    server.stubFor(post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderRef))).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    return orderRef;
  }

  public static String pendingCollect(final String orderReference, final CollectResponse.Status status) {
    String response = """
        {
          "orderRef": "%s",
          "autoStartToken": "%s",
          "qrStartToken": "%s",
          "qrStartSecret": "%s",
          "status": "%s"
        }
        """.formatted(
        orderReference,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        status.getValue()
    );
    server.stubFor(post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderReference))).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    return orderReference;
  }

  public static String completeCollect(final String orderReference) throws JsonProcessingException {
    CompletionData completionData = new CompletionData();
    completionData.setSignature("signature");
    CompletionData.User user = new CompletionData.User();
    user.setName("Test");
    user.setGivenName("Test Test");
    user.setSurname("Test");
    user.setPersonalNumber("200001011111");
    completionData.setUser(user);
    ObjectMapper objectMapper = new ObjectMapper();
    String response = """
        {
          "orderRef": "%s",
          "autoStartToken": "%s",
          "qrStartToken": "%s",
          "qrStartSecret": "%s",
          "status": "%s",
          "completionData": %s
        }
        """.formatted(orderReference, UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), CollectResponse.Status.COMPLETE.getValue(), objectMapper.writerFor(CompletionData.class).writeValueAsString(completionData));
    server.stubFor(post("/collect").withRequestBody(matchingJsonPath("$.orderRef", equalTo(orderReference))).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    return orderReference;
  }
}
