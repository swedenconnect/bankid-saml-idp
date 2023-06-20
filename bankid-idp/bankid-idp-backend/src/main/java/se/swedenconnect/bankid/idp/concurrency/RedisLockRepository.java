package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Handles locks for critical section distributed over redis
 */
@AllArgsConstructor
public class RedisLockRepository implements LockRepository {
  private final RedissonClient client;


  @Override
  public Lock get(String key) {
    RLock lock = client.getLock(key);
    try {
      // Do not keep the lock forever even if it is not unlocked
      boolean acquired = lock.tryLock(0, 2, TimeUnit.SECONDS);
      if (acquired) {
        return lock;
      }
      throw new LockAlreadyAcquiredException();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Thread was interrupted whilst waiting for lock");
    }
  }
}
