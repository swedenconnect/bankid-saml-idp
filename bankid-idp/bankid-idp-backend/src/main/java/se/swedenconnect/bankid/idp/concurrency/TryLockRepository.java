package se.swedenconnect.bankid.idp.concurrency;

import java.util.concurrent.locks.Lock;

public interface TryLockRepository {
  /**
   * Locking repository responsible for providing locks by key
   *
   * @param key Unique identifier for the lock
   * @return A lock
   */
  TryLock get(String key);
}
