package se.swedenconnect.bankid.idp.concurrency;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Implements a Subset of lock with only trylock / unlock
 */
public interface TryLock {
  boolean tryLock();
  void unlock();

  static TryLock create(Supplier<Boolean> tryLock, Runnable unlock) {
    return new TryLock() {
      @Override
      public boolean tryLock() {
        return tryLock.get();
      }

      @Override
      public void unlock() {
        unlock.run();
      }
    };
  }
}
