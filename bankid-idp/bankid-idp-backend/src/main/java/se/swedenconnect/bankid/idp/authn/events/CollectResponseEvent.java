package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Data
public class CollectResponseEvent {
  private final HttpServletRequest request;
  private final CollectResponse collectResponse;
}
