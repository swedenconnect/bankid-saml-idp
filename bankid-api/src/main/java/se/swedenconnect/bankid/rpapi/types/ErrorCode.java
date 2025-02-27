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
package se.swedenconnect.bankid.rpapi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enumeration for all error codes, both those received as {@code hintCode} in collect responses and and as
 * {@code errorCode} in error responses.
 *
 * @author Martin Lindström
 */
public enum ErrorCode {

  /** HintCode - expiredTransaction */
  EXPIRED_TRANSACTION("expiredTransaction"),

  /** HintCode - certificateErr */
  CERTIFICATE_ERR("certificateErr"),

  /** HintCode - userCancel */
  USER_CANCEL("userCancel"),

  /** HintCode - cancelled */
  CANCELLED("cancelled"),

  /** HintCode - startFailed */
  START_FAILED("startFailed"),

  /** ErrorCode - alreadyInProgress */
  ALREADY_IN_PROGRESS("alreadyInProgress"),

  /** ErrorCode - invalidParameters */
  INVALID_PARAMETERS("invalidParameters"),

  /** ErrorCode - unauthorized */
  UNAUTHORIZED("unauthorized"),

  /** ErrorCode - notFound */
  NOT_FOUND("notFound"),

  /** ErrorCode - requestTimeout */
  REQUEST_TIMEOUT("requestTimeout"),

  /** ErrorCode - unsupportedMediaType */
  UNSUPPORTED_MEDIA_TYPE("unsupportedMediaType"),

  /** ErrorCode - internalError */
  INTERNAL_ERROR("internalError"),

  /** ErrorCode - Maintenance */
  MAINTENANCE("Maintenance"),

  /** Last resort. */
  UNKNOWN_ERROR("unknown");

  /** The string representation of the enumeration. */
  private String value;

  /**
   * Constructor.
   *
   * @param value enum string representation
   */
  ErrorCode(final String value) {
    this.value = value;
  }

  /**
   * Given a string representation its enum object is returned.
   *
   * @param value the string representation
   * @return a {@code ErrorCode}
   */
  @JsonCreator
  public static ErrorCode forValue(final String value) {
    for (final ErrorCode e : ErrorCode.values()) {
      if (e.getValue().equalsIgnoreCase(value)) {
        return e;
      }
    }
    return ErrorCode.UNKNOWN_ERROR;
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
