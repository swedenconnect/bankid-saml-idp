package se.swedenconnect.bankid.idp.concurrency;

import java.util.concurrent.locks.Lock;

public interface LockRepository {
  /**
   * Locking repository will acquire the lock OR throw an IllegalStateException
   * if the lock was already acquired.
   *
   * @param key Unique user identifier for the lock
   * @return An acquired lock
   */
  Lock get(String key);
}
