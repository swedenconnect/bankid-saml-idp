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
package se.swedenconnect.bankid.idp.authn.error;

import se.swedenconnect.bankid.idp.ApplicationVersion;

/**
 * Base class for all BankID exceptions.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public abstract class BankIdException extends RuntimeException {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  /**
   * Default constructor.
   */
  protected BankIdException() {
    super();
  }

  /**
   * Constructor taking an error message.
   * 
   * @param msg the error message
   */
  protected BankIdException(final String msg) {
    super(msg);
  }

  /**
   * Constructor taking an error message and the cause of the error.
   * 
   * @param msg the error message
   * @param cause the cause of the error
   */
  protected BankIdException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

}
