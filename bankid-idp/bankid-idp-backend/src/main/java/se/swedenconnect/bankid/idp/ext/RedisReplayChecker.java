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

import lombok.AllArgsConstructor;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import se.swedenconnect.spring.saml.idp.authnrequest.validation.AbstractMessageReplayChecker;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.SortedSet;

/**
 * ReplayChecker Using Two Redis Sorted Set
 * One for replays and one for expiration
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
public class RedisReplayChecker extends AbstractMessageReplayChecker {

  public static final Duration TIME_TO_LIVE = Duration.of(5, ChronoUnit.MINUTES);
  /*
   * The client for redisson
   */
  private final RedissonClient client;

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean existsInCache(String s) {
    removeExpired();
    return getRedisSortedSet().contains(s);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addToCache(String s) {
    removeExpired();
    addEntry(s);
  }

  /**
   * @param s id to add
   */

  private void addEntry(String s) {
    SortedSet<String> redisSortedSet = getRedisSortedSet();
    RScoredSortedSet<String> expirationsSortedSet = getExpirationsSortedSet();
    redisSortedSet.add(s);
    expirationsSortedSet.add(Instant.now().plus(TIME_TO_LIVE).getEpochSecond(), s);
  }

  /**
   * Remove all entries that has expired
   */
  private void removeExpired() {
    SortedSet<String> redisSortedSet = getRedisSortedSet();
    RScoredSortedSet<String> expirationsSortedSet = getExpirationsSortedSet();
    Collection<String> expiredEntries = expirationsSortedSet.valueRange(Instant.EPOCH.getEpochSecond(), true, Instant.now().getEpochSecond(), true);
    expirationsSortedSet.removeAll(expiredEntries);
    redisSortedSet.removeAll(expiredEntries);
  }

  /*
   * Method that returns the sorted set used for the replay checker
   */
  private SortedSet<String> getRedisSortedSet() {
    return client.getLexSortedSet("replaychecker");
  }

  /*
   * Method that returns time to live for replaychecker
   */
  private RScoredSortedSet<String> getExpirationsSortedSet() {
    return client.getScoredSortedSet("replaychecker_ttl");
  }
}
