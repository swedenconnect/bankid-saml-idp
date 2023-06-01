package se.swedenconnect.bankid.idp.authn;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;

@Data
public class BankIdSessionExpiredException extends RuntimeException {

  private final HttpServletRequest expiredSessionHolder;

  public BankIdSessionExpiredException(HttpServletRequest expiredSessionHolder) {
    super("The session towards bankid has timed out");
    this.expiredSessionHolder = expiredSessionHolder;
  }
}
