package se.swedenconnect.bankid.idp.authn.events;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AbortAuthEvent {
  private final HttpServletRequest request;
}
