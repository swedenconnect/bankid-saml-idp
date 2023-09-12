package se.swedenconnect.bankid.idp.integration;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import se.swedenconnect.bankid.idp.integration.response.CollectResponseBuilder;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import java.util.UUID;
import java.util.function.Function;

public class BankIdResponseFactory {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static OrderResponse start() {
    OrderResponse orderResponse = new OrderResponse();
    orderResponse.setOrderReference(UUID.randomUUID().toString());
    orderResponse.setQrStartSecret(UUID.randomUUID().toString());
    orderResponse.setQrStartToken(UUID.randomUUID().toString());
    orderResponse.setAutoStartToken(UUID.randomUUID().toString());
    return orderResponse;
  }

  public static CollectResponse collect(OrderResponse orderResponse) {
    return collect(orderResponse, c -> c.status(CollectResponse.Status.PENDING).hintCode("NO_CLIENT"));
  }

  public static String serialize(Object object) {
    try {
      if (object instanceof OrderResponse orderResponse) {
        return mapper.writerFor(OrderResponse.class).writeValueAsString(orderResponse);
      }
      if (object instanceof CollectResponse collectResponse) {
        return mapper.writerFor(CollectResponse.class).writeValueAsString(collectResponse);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    throw new IllegalArgumentException("Object Subclass is not supported class:" + object.getClass().getCanonicalName());
  }

  public static CollectResponse failStart(OrderResponse toFail) {
    return collect(toFail, c -> c.hintCode("startFailed").status(CollectResponse.Status.FAILED));
  }

  public static CollectResponse collect(OrderResponse orderResponse, Function<CollectResponseBuilder, CollectResponseBuilder> customizer) {
    CollectResponseBuilder builder = new CollectResponseBuilder()
        .orderReference(orderResponse.getOrderReference())
        .status(CollectResponse.Status.PENDING);
    return customizer.apply(builder).build();
  }

  public static CollectResponse complete(OrderResponse orderResponse) {
    CompletionData completionData = new CompletionData();
    completionData.setSignature("signature");
    CompletionData.User user = new CompletionData.User();
    user.setName("Test");
    user.setGivenName("Test Test");
    user.setSurname("Test");
    user.setPersonalNumber("200001011111");
    completionData.setUser(user);
    return collect(orderResponse, c -> c
        .completionData(completionData)
        .status(CollectResponse.Status.COMPLETE));
  }
}
