package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Data
public class OrderCancellationEvent {
  private final HttpServletRequest request;
  private final RelyingPartyData data;
}
