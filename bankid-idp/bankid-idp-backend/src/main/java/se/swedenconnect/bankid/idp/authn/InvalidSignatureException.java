package se.swedenconnect.bankid.idp.authn;

/**
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class InvalidSignatureException extends BankIdAuthenticationException {
  public InvalidSignatureException(String identifier) {
    super(identifier);
  }
}
