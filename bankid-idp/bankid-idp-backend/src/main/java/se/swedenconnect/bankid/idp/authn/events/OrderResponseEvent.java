package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Data
public class OrderResponseEvent {
  private final HttpServletRequest request;
  private final OrderResponse orderResponse;

  private final Boolean showQr;
}
