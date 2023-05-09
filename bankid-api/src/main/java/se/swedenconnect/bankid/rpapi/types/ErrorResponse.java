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
package se.swedenconnect.bankid.rpapi.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representation of an error response message.
 *
 * @author Martin Lindström
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

  /** The error code. */
  private ErrorCode errorCode;

  /** Error details. */
  private String details;

  /**
   * Default constructor.
   */
  public ErrorResponse() {
  }

  /**
   * Constructor.
   *
   * @param errorCode the error code
   * @param details the error details
   */
  public ErrorResponse(final ErrorCode errorCode, final String details) {
    this.errorCode = errorCode;
    this.details = details;
  }

  /**
   * Returns the error code.
   *
   * @return the error code
   */
  public ErrorCode getErrorCode() {
    return this.errorCode;
  }

  /**
   * Assigns the error code.
   *
   * @param errorCode the error code
   */
  public void setErrorCode(final ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Returns the error details.
   *
   * @return the error details
   */
  public String getDetails() {
    return this.details;
  }

  /**
   * Assigns the error details.
   *
   * @param details the error details
   */
  public void setDetails(final String details) {
    this.details = details;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("errorCode='%s', details='%s'",
        this.errorCode != null ? this.errorCode.getValue() : "<not set>", this.details);
  }

}
