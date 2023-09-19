/*
 * Copyright 2023 Sweden Connect
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
package se.swedenconnect.bankid.idp.ext;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.SortedSet;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import se.swedenconnect.spring.saml.idp.authnrequest.validation.AbstractMessageReplayChecker;

/**
 * ReplayChecker using two Redis sorted set. One for replays and one for expiration.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RedisReplayChecker extends AbstractMessageReplayChecker {

  /** The client for redisson. */
  private final RedissonClient client;

  /**
   * Constructor.
   *
   * @param client the Redis client
   */
  public RedisReplayChecker(final RedissonClient client) {
    this.client = Objects.requireNonNull(client, "client must not be null");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean existsInCache(final String s) {
    this.removeExpired();
    return this.getRedisSortedSet().contains(s);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addToCache(final String s) {
    this.removeExpired();
    this.addEntry(s);
  }

  /**
   * @param s id to add
   */

  private void addEntry(final String s) {
    final SortedSet<String> redisSortedSet = this.getRedisSortedSet();
    final RScoredSortedSet<String> expirationsSortedSet = this.getExpirationsSortedSet();
    redisSortedSet.add(s);
    expirationsSortedSet.add(Instant.now().plus(this.replayCacheExpiration).getEpochSecond(), s);
  }

  /**
   * Remove all entries that has expired
   */
  private void removeExpired() {
    final SortedSet<String> redisSortedSet = this.getRedisSortedSet();
    final RScoredSortedSet<String> expirationsSortedSet = this.getExpirationsSortedSet();
    final Collection<String> expiredEntries =
        expirationsSortedSet.valueRange(Instant.EPOCH.getEpochSecond(), true, Instant.now().getEpochSecond(), true);
    expirationsSortedSet.removeAll(expiredEntries);
    redisSortedSet.removeAll(expiredEntries);
  }

  /*
   * Method that returns the sorted set used for the replay checker
   */
  private SortedSet<String> getRedisSortedSet() {
    return this.client.getLexSortedSet("replaychecker");
  }

  /*
   * Method that returns time to live for replaychecker
   */
  private RScoredSortedSet<String> getExpirationsSortedSet() {
    return this.client.getScoredSortedSet("replaychecker_ttl");
  }
}
