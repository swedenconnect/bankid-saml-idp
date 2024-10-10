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

package se.swedenconnect.bankid.idp.authn.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enumeration for BankID security violation errors.
 *
 * @author Martin Lindström
 */
public enum BankIdSecurityViolationError {

  /**
   * If the received nonce value differs from the expected.
   */
  NONCE_MISMATCH("nonce-mismatch"),

  /**
   * We did not receive a nonce value (when it was expected).
   */
  NONCE_MISSING("missing-nonce");

  /**
   * Constructor.
   *
   * @param value the string representation
   */
  BankIdSecurityViolationError(final String value) {
    this.value = value;
  }

  /** The string representation of the error. */
  private final String value;

  /**
   * Given a string representation its enum object is returned.
   *
   * @param value the string representation
   * @return a {@code BankIdSecurityViolationError}
   */
  @JsonCreator
  public static BankIdSecurityViolationError forValue(final String value) {
    for (final BankIdSecurityViolationError e : BankIdSecurityViolationError.values()) {
      if (e.getValue().equalsIgnoreCase(value)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Unknown error: " + value);
  }

  /**
   * Returns the string representation of the enum.
   *
   * @return the string representation
   */
  @JsonValue
  public String getValue() {
    return this.value;
  }

}
