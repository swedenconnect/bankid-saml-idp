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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.util.StringUtils;

import se.swedenconnect.spring.saml.idp.audit.Saml2AuditEvents;

/**
 * The base {@link AuditEventRepository} for the BankID IdP.
 *
 * @deprecated Use the Audit support from the SAML IdP project instead
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Deprecated(forRemoval = true, since = "1.1.3")
public abstract class AbstractBankIdAuditEventRepository implements AuditEventRepository, DisposableBean {

  /** Logger. */
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(AbstractBankIdAuditEventRepository.class);

  /**
   * The events that we support by default.
   */
  public static List<String> DEFAULT_SUPPORTED_EVENTS = Stream.concat(
      Arrays.stream(Saml2AuditEvents.values())
          .map(Saml2AuditEvents::getTypeName)
          .toList().stream(),
      Arrays.stream(BankIdAuditEventTypes.values())
          .map(BankIdAuditEventTypes::getTypeName)
          .toList().stream())
      .toList();

  /** The supported events. */
  private final List<String> supportedEvents;

  /** The underlying JUL handler. */
  private final DateRollingFileHandler handler;

  /** The JUL logger. */
  private final Logger auditLogger;

  /** The mapper that writes JSON. */
  private final AuditEventMapper mapper;

  /**
   * Constructor.
   *
   * @param logFile the log file including its path (if {@code null}, no file logging will be performed)
   * @param mapper mapper for creating JSON
   * @param supportedEvents the supported events (if {@code null}, {@link #DEFAULT_SUPPORTED_EVENTS} will be used)
   * @throws IOException if file logging can not be initialized
   */
  public AbstractBankIdAuditEventRepository(
      final String logFile, final AuditEventMapper mapper, final List<String> supportedEvents)
      throws IOException {
    this.supportedEvents = Optional.ofNullable(supportedEvents).orElseGet(() -> DEFAULT_SUPPORTED_EVENTS);
    this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");

    this.handler = StringUtils.hasText(logFile) ? new DateRollingFileHandler(logFile) : null;
    if (this.handler != null) {
      // Build the logger name based on the log file name ...
      final String loggerName = Path.of(logFile).toAbsolutePath().toString();

      this.auditLogger = Logger.getLogger(loggerName);
      this.auditLogger.setLevel(Level.INFO);
      this.auditLogger.addHandler(this.handler);
      this.auditLogger.setUseParentHandlers(false);
    }
    else {
      this.auditLogger = null;
    }

  }

  /** {@inheritDoc} */
  @Override
  public final void add(final AuditEvent event) {
    if (event == null) {
      return;
    }

    if (this.supportedEvents.contains(event.getType())) {
      log.info("Audit logging event '{}' for principal '{}' ...", event.getType(), event.getPrincipal());

      if (this.auditLogger != null) {
        try {
          this.auditLogger.log(Level.INFO, this.mapper.write(event));
        }
        catch (final Throwable e) {
          log.error("Failed to audit log to file - {}", e.getMessage(), e);
        }
      }
      this.addEvent(event);
    }
  }

  /**
   * Logs an event.
   *
   * @param event the audit event to log
   */
  protected abstract void addEvent(final AuditEvent event);

  /** {@inheritDoc} */
  @Override
  public void destroy() throws Exception {
    if (this.handler != null) {
      this.handler.flush();
      this.handler.close();
    }
  }

  /**
   * Returns the audit event mapper.
   *
   * @return the {@link AuditEventMapper}
   */
  protected AuditEventMapper getAuditEventMapper() {
    return this.mapper;
  }

}
