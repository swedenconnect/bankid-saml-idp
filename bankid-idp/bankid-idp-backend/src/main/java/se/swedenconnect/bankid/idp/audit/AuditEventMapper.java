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
package se.swedenconnect.bankid.idp.audit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.actuate.audit.AuditEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.swedenconnect.bankid.idp.ApplicationVersion;

/**
 * Wrapper for ObjectMapper to handle AuditEvent.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class AuditEventMapper {

  /** The underlying {@link ObjectMapper}. */
  private final ObjectMapper mapper;

  /**
   * Constructor.
   *
   * @param mapper the {@link ObjectMapper}
   */
  public AuditEventMapper(final ObjectMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
  }

  /**
   * Serializes AuditEvent to JSON.
   *
   * @param event to serialize
   * @return json-string
   */
  public String write(final AuditEvent event) {
    try {
      return this.mapper.writerFor(AuditEvent.class).writeValueAsString(event);
    }
    catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Deserializes AuditEvent from json
   *
   * @param event to deserialize
   * @return AuditEvent
   */

  public AuditEvent read(final String event) {
    try {
      // Read BankidAuditEvent which extends AuditEvent with @JsonCreator and cast to AuditEvent
      final BankidAuditEvent auditEvent = this.mapper.readerFor(BankidAuditEvent.class).readValue(event);
      return auditEvent;
    }
    catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Helper class for reading events.
   */
  private static class BankidAuditEvent extends AuditEvent {

    private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

    /**
     * Adds a JsonCreator for Jackson to be able to serialize AuditEvnets
     *
     * @param principal to deserialize
     * @param type to deserialize
     * @param data to deserialize
     */
    @JsonCreator
    public BankidAuditEvent(@JsonProperty("principal") final String principal, @JsonProperty("type") final String type,
        @JsonProperty("data") final Map<String, Object> data) {
      super(principal, type, Optional.ofNullable(data).orElse(Map.of()));
    }
  }
}
