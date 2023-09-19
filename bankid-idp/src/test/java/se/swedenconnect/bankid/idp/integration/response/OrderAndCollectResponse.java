package se.swedenconnect.bankid.idp.integration.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

@AllArgsConstructor
@Data
public class OrderAndCollectResponse {
  private final OrderResponse orderResponse;
  private final CollectResponse collectResponse;
}
