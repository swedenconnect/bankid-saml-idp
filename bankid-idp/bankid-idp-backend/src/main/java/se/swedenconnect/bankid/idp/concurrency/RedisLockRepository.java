package se.swedenconnect.bankid.idp.concurrency;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@AllArgsConstructor
@Component
public class RedisLockRepository implements LockRepository {
  private final RedissonClient client;


  @Override
  public Lock get(String key) {
    return client.getLock(key);
  }
}
