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
package se.swedenconnect.bankid.idp.audit;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.redisson.api.RedissonClient;
import org.redisson.api.TimeSeriesEntry;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;

import se.swedenconnect.spring.saml.idp.audit.repository.RedissonTimeSeriesAuditEventRepository;

/**
 * An {@link AuditEventRepository} implementation that uses Redis time series to store events.
 *
 * @deprecated Use {@link RedissonTimeSeriesAuditEventRepository} instead.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Deprecated(forRemoval = true, since = "1.1.3")
public class RedisTimeSeriesAuditEventRepository extends AbstractBankIdAuditEventRepository {

  /** The Redis client. */
  private final RedissonClient client;

  /**
   * Constructor.
   *
   * @param client the Redis client
   * @param logFile the log file including its path (if {@code null}, no file logging will be performed)
   * @param mapper mapper for creating JSON
   * @param supportedEvents the supported events (if {@code null}, {@link #DEFAULT_SUPPORTED_EVENTS} will be used)
   * @throws IOException if file logging can not be initialized
   */
  public RedisTimeSeriesAuditEventRepository(final RedissonClient client, final String logFile,
      final AuditEventMapper mapper, final List<String> supportedEvents)
      throws IOException {
    super(logFile, mapper, supportedEvents);
    this.client = Objects.requireNonNull(client, "client must not be null");
  }

  /** {@inheritDoc} */
  @Override
  protected void addEvent(final AuditEvent event) {
    this.client.getTimeSeries("audit:ts")
        .add(event.getTimestamp().toEpochMilli(), this.getAuditEventMapper().write(event));
  }

  /** {@inheritDoc} */
  @Override
  public List<AuditEvent> find(final String principal, final Instant after, final String type) {
    final Collection<TimeSeriesEntry<Object, Object>> timeSeries =
        this.client.getTimeSeries("audit:ts").entryRange(
            Optional.ofNullable(after)
                .orElseGet(() -> Instant.EPOCH)
                .toEpochMilli(),
            Instant.now().plus(1, ChronoUnit.MINUTES).toEpochMilli());

    final Stream<AuditEvent> auditEventStream = timeSeries.stream()
        .map(e -> this.getAuditEventMapper().read((String) e.getValue()));
    if (Objects.nonNull(principal)) {
      return auditEventStream.filter(event -> principal.equals(event.getPrincipal())).toList();
    }
    return auditEventStream.toList();
  }

}
