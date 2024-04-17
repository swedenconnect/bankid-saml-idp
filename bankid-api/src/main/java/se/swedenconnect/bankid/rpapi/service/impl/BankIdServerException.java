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
package se.swedenconnect.bankid.rpapi.service.impl;

import se.swedenconnect.bankid.rpapi.LibraryVersion;
import se.swedenconnect.bankid.rpapi.types.BankIDException;

/**
 * Exception class for 5XX API errors.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdServerException extends BankIDException {

  /** For serializing. */
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /**
   * Constructor.
   *
   * @param message the error message
   */
  public BankIdServerException(final String message) {
    super(message);
  }
}
