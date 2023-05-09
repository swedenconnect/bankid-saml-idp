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
 * An enum representing a previous selection of device.
 */
public enum PreviousDeviceSelection {

  /** This device. */
  THIS_DEVICE("this"),

  /** BankID signature. */
  OTHER("other");

  /** The string representation of the enum. */
  private String value;

  /**
   * Constructor.
   * 
   * @param value the string representation of the enum
   */
  private PreviousDeviceSelection(final String value) {
    this.value = value;
  }

  /**
   * Given a string representation its enum object is returned.
   * 
   * @param value the string representation
   * @return a {@link PreviousDeviceSelection}
   */
  @JsonCreator
  public static PreviousDeviceSelection forValue(String value) {
    for (final PreviousDeviceSelection p : PreviousDeviceSelection.values()) {
      if (p.getValue().equalsIgnoreCase(value)) {
        return p;
      }
    }
    throw new IllegalArgumentException("Unknown PreviousDeviceSelection - " + value);
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
