package se.swedenconnect.bankid.idp.authn;

import lombok.Data;
import lombok.EqualsAndHashCode;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;

import javax.servlet.http.HttpServletRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankIdSessionExpiredException extends RuntimeException {

  private final PollRequest request;

  public BankIdSessionExpiredException(final PollRequest request) {
    super("The session towards bankid has timed out");
    this.request = request;
  }
}
