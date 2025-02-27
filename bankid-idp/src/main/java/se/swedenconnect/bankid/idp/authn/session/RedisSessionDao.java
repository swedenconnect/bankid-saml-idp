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
package se.swedenconnect.bankid.idp.authn.session;

import java.time.Instant;
import java.util.Objects;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Redis implementation of the {@link SessionDao} interface.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RedisSessionDao implements SessionDao {

  private final RedissonClient client;

  /**
   * Constructor.
   *
   * @param client the Redis client
   */
  public RedisSessionDao(final RedissonClient client) {
    this.client = Objects.requireNonNull(client, "client must not be null");
  }

  /** {@inheritDoc} */
  @Override
  public void write(final String key, final Object value, final HttpServletRequest request) {
    final RMap<Object, Object> map = this.getRedisHashForUser(request);
    map.fastPut(key, value);
    map.expire(Instant.now().plusSeconds(request.getSession().getMaxInactiveInterval()));
  }

  /** {@inheritDoc} */
  @Override
  public <T> T read(final String key, final Class<T> tClass, final HttpServletRequest request) {
    final RMap<Object, Object> map = this.getRedisHashForUser(request);
    return tClass.cast(map.get(key));
  }

  /** {@inheritDoc} */
  @Override
  public void remove(final String key, final HttpServletRequest request) {
    final RMap<Object, Object> map = this.getRedisHashForUser(request);
    map.remove(key);
  }

  /** {@inheritDoc} */
  private RMap<Object, Object> getRedisHashForUser(final HttpServletRequest request) {
    return this.client.getMap("session:%s".formatted(request.getSession().getId()));
  }

}
