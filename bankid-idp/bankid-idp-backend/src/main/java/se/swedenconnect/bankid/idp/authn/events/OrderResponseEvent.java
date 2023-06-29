package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.OrderResponse;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Data
public class OrderResponseEvent {
  private final PollRequest request;
  private final OrderResponse response;
}
