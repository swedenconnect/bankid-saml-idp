package se.swedenconnect.bankid.idp.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class is not meant for production use
 */
public class InMemoryTryLockRepository implements TryLockRepository {

  private final Map<String, Lock> stringLockMap = new ConcurrentHashMap<>();

  @Override
  public TryLock get(String key) {
    Lock lock = stringLockMap.computeIfAbsent(key, s -> new ReentrantLock());
    return TryLock.create(lock::tryLock, lock::unlock);
  }
}
