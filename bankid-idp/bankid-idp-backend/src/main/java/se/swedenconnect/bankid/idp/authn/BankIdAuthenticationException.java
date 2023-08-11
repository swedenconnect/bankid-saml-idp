package se.swedenconnect.bankid.idp.authn;

import lombok.Data;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Data
public class BankIdAuthenticationException extends RuntimeException {
  private final String identifier;

  public BankIdAuthenticationException(String identifier) {
    super();
    this.identifier = identifier;
  }
}
