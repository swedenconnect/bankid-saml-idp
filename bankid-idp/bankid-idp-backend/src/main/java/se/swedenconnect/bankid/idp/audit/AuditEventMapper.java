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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Wrapper for ObjectMapper to handle AuditEvent
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
public class AuditEventMapper {

  private final ObjectMapper mapper;

  /**
   * Serializes AuditEvent to json
   * @param event to serialize
   * @return json-string
   */
  public String write(AuditEvent event) {
    try {
      return mapper.writerFor(AuditEvent.class).writeValueAsString(event);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Deserializes AuditEvent from json
   * @param event to deserialize
   * @return AuditEvent
   */

  public AuditEvent read(String event) {
    try {
      // Read BankidAuditEvent which extends AuditEvent with @JsonCreator and cast to AuditEvent
      return mapper.readerFor(BankidAuditEvent.class).readValue(event);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static class BankidAuditEvent extends AuditEvent {
    /**
     * Adds a JsonCreator for Jackson to be able to serialize AuditEvnets
     * @param principal to deserialize
     * @param type to deserialize
     * @param data to deserialize
     */

    @JsonCreator
    public BankidAuditEvent(@JsonProperty("principal") String principal, @JsonProperty("type") String type, @JsonProperty("data") Map<String, Object> data) {
      super(principal, type, data);
    }
  }
}
