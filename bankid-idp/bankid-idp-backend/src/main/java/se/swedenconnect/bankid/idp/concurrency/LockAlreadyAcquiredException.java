package se.swedenconnect.bankid.idp.concurrency;

public class LockAlreadyAcquiredException  extends IllegalStateException {
  public LockAlreadyAcquiredException() {
    super("Lock was already acquired for current user");
  }
}
