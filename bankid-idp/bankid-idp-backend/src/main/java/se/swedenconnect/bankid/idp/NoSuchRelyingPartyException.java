package se.swedenconnect.bankid.idp;

public class NoSuchRelyingPartyException extends RuntimeException {
  public NoSuchRelyingPartyException() {
    super("Not registered");
  }
}
