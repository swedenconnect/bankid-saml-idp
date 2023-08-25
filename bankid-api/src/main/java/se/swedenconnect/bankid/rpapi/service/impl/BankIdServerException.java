package se.swedenconnect.bankid.rpapi.service.impl;

import se.swedenconnect.bankid.rpapi.types.BankIDException;

public class BankIdServerException extends BankIDException {
  public BankIdServerException(String message) {
    super(message);
  }
}
