package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.Lock;

@AllArgsConstructor
public class RedisLockRepository implements LockRepository {
  private final RedissonClient client;


  @Override
  public Lock get(String key) {
    return client.getLock(key);
  }
}
