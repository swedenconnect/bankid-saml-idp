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
package se.swedenconnect.bankid.idp.authn.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of an API response message.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse {

  /**
   * Representation of the status of the response.
   */
  public enum Status {

    /** The operation against the BankID Server has not been started. */
    NOT_STARTED,

    /** The response is in progress. */
    IN_PROGRESS,

    /** Error response. */
    ERROR,

    /** The operation has been completed. */
    COMPLETE,

    /** The operation has been cancelled. */
    CANCEL
  }

  /** The status code for the response. */
  private Status status;

  /** The QR code. */
  private String qrCode;

  /** The autostart token. */
  private String autoStartToken;

  /** The code for the detailed message. */
  private String messageCode;
}
