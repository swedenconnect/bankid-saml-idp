package se.swedenconnect.bankid.idp.authn.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Data
public class AbortAuthEvent {
  private final HttpServletRequest request;
}
