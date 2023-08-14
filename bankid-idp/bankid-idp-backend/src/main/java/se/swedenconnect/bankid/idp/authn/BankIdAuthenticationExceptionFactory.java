package se.swedenconnect.bankid.idp.authn;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
/**
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Slf4j
public class BankIdAuthenticationExceptionFactory {
  public static InvalidSignatureException invalidSignature(String orderReference) {
    String randomIdentifier = UUID.randomUUID().toString();
    log.error("BankIdAuthenticationException created for orderReference:{} with identifier:{}", orderReference, randomIdentifier);
    return new InvalidSignatureException(randomIdentifier);
  }

  public static BankIdAuthenticationException validationError(String orderReference) {
    String randomIdentifier = UUID.randomUUID().toString();
    log.error("BankIdAuthenticationException created for orderReference:{} with identifier:{}", orderReference, randomIdentifier);
    return new BankIdAuthenticationException(randomIdentifier);
  }
}
