/*
 * Copyright 2023-2024 Sweden Connect
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
import java.util.List;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;

/**
 * A memory based {@link AuditEventRepository}.
 *
 * @deprecated Use {@link se.swedenconnect.spring.saml.idp.audit.repository.MemoryBasedAuditEventRepository} instead
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Deprecated(forRemoval = true, since = "1.1.3")
public class MemoryBasedAuditEventRepository extends AbstractBankIdAuditEventRepository {

  /** The underlying in-memory repository. */
  private final InMemoryAuditEventRepository repository;

  /**
   * Constructor.
   *
   * @param logFile the log file including its path (if {@code null}, no file logging will be performed)
   * @param mapper mapper for creating JSON
   * @param supportedEvents the supported events (if {@code null}, {@link #DEFAULT_SUPPORTED_EVENTS} will be used)
   * @throws IOException if file logging can not be initialized
   */
  public MemoryBasedAuditEventRepository(
      final String logFile, final AuditEventMapper mapper, final List<String> supportedEvents)
      throws IOException {
    super(logFile, mapper, supportedEvents);
    this.repository = new InMemoryAuditEventRepository();
  }

  /** {@inheritDoc} */
  @Override
  public List<AuditEvent> find(final String principal, final Instant after, final String type) {
    return this.repository.find(principal, after, type);
  }

  /** {@inheritDoc} */
  @Override
  protected void addEvent(final AuditEvent event) {
    this.repository.add(event);
  }

}
