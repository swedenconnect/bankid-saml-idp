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
package se.swedenconnect.bankid.rpapi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enumeration representing the {@code hintCode} passed in a collect response when the status is pending. The
 * enumeration also represents "complete".
 *
 * @author Martin Lindström
 */
public enum ProgressStatus {

  /**
   * The order is being processed. The client has not yet received the order. The status will later change to NO_CLIENT,
   * STARTED or USER_SIGN.
   */
  OUTSTANDING_TRANSACTION("outstandingTransaction"),

  /**
   * The order is being processed. The client has not yet received the order.
   */
  NO_CLIENT("noClient"),

  /**
   * The order is pending. A client has been started with the autostarttoken but a usable ID has not yet been found in
   * the started client.
   */
  STARTED("started"),

  /**
   * Order is pending. A client has launched and received the order but additional steps for providing MRTD information
   * is required to proceed with the order.
   */
  USER_MRTD("userMrtd"),

  /**
   * The client has received the order.
   */
  USER_SIGN("userSign"),

  /**
   * An unknown hint code was received in the collect response.
   */
  UNKNOWN_HINTCODE("unknown_hintcode"),

  /**
   * The user has provided the security code and completed the order. Collect response includes the signature, user
   * information and the ocsp response.
   */
  COMPLETE("complete");

  /** The string representation of the enum. */
  private String value;

  /**
   * Constructor.
   *
   * @param value the string representation of the enum
   */
  ProgressStatus(final String value) {
    this.value = value;
  }

  /**
   * Given a string representation its enum object is returned.
   *
   * @param value the string representation
   * @return a {@code ProgressStatus}
   */
  @JsonCreator
  public static ProgressStatus forValue(final String value) {
    for (final ProgressStatus p : ProgressStatus.values()) {
      if (p.getValue().equalsIgnoreCase(value)) {
        return p;
      }
    }
    return ProgressStatus.UNKNOWN_HINTCODE;
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
