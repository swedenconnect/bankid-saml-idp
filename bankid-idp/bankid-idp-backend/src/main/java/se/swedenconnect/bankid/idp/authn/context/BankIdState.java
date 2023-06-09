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
package se.swedenconnect.bankid.idp.authn.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enum representing the state a BankID operation can be in.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public enum BankIdState {

  /** Init state. */
  INIT("init"),

  /** Waiting for app start. */
  WAIT("wait");

  /** The string representation of the enum. */
  private String value;

  /**
   * Constructor.
   * 
   * @param value the string representation of the enum
   */
  private BankIdState(final String value) {
    this.value = value;
  }

  /**
   * Given a string representation its enum object is returned.
   * 
   * @param value the string representation
   * @return a {@link BankIdState}
   */
  @JsonCreator
  public static BankIdState forValue(final String value) {
    for (final BankIdState s : BankIdState.values()) {
      if (s.getValue().equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unknown BankIdState - " + value);
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
