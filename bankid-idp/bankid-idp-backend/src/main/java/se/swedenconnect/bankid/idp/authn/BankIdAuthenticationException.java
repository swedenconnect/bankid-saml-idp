package se.swedenconnect.bankid.idp.authn;

import lombok.Data;

@Data
public class BankIdAuthenticationException extends RuntimeException {
  private final String identifier;

  public BankIdAuthenticationException(String identifier) {
    super();
    this.identifier = identifier;
  }
}
