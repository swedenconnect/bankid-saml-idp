package se.swedenconnect.bankid.idp.authn;

public class InvalidSignatureException extends BankIdAuthenticationException {
  public InvalidSignatureException(String identifier) {
    super(identifier);
  }
}
