package se.swedenconnect.bankid.rpapi.service.impl;

import se.swedenconnect.bankid.rpapi.types.BankIDException;

public class BankIdUserException extends BankIDException {
  public BankIdUserException(String message) {
    super(message);
  }
}
