package se.swedenconnect.bankid.idp.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class InMemoryLockRepository implements LockRepository {

  private final Map<String, Lock> stringLockMap = new ConcurrentHashMap<>();

  @Override
  public Lock get(String key) {
    return stringLockMap.computeIfAbsent(key, s -> new ReentrantLock());
  }
}
