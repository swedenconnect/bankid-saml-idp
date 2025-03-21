/*
 * Copyright 2023-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.concurrency;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * A Redis implementation of {@link TryLockRepository}. Provides locks, distributed over Redis.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RedisTryLockRepository implements TryLockRepository {

  /** The Redis client. */
  private final RedissonClient client;

  /**
   * Constructor.
   *
   * @param client the Redis client
   */
  public RedisTryLockRepository(final RedissonClient client) {
    this.client = Objects.requireNonNull(client, "client must not be null");
  }

  /** {@inheritDoc} */
  @Override
  public TryLock get(final String key) {
    final RLock lock = this.client.getLock(key);
    return TryLock.create(getTryLock(lock), lock::unlock);
  }

  /**
   * Creates a redis TryLock that automatically expires after a set time
   *
   * @param lock The Lock provided from redis
   * @return TryLock Supplier for the lock
   */
  private static Supplier<Boolean> getTryLock(final RLock lock) {
    return () -> {
      try {
        return lock.tryLock(0, 2, TimeUnit.SECONDS);
      }
      catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Thread was interrupted whilst waiting for lock");
      }
    };
  }

}
