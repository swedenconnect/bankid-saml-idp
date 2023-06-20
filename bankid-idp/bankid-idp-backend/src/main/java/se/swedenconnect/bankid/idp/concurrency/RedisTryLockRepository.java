package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Provides locks, distributed over redis
 */
@AllArgsConstructor
public class RedisTryLockRepository implements TryLockRepository {
  private final RedissonClient client;


  @Override
  public TryLock get(String key) {
    RLock lock = client.getLock(key);
    return TryLock.create(getTryLock(lock), lock::unlock);
  }

  /**
   * Creates a redis TryLock that automatically expires after a set time
   * @param lock The Lock provided from redis
   * @return TryLock Supplier for the lock
   */
  private static Supplier<Boolean> getTryLock(RLock lock) {
    return () -> {
      try {
        return lock.tryLock(0, 2, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Thread was interrupted whilst waiting for lock");
      }
    };
  }
}
