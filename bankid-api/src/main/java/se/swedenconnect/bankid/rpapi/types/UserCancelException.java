/*
 * Copyright 2023 Litsec AB
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

import se.swedenconnect.bankid.rpapi.LibraryVersion;

/**
 * Exception class for when the user cancels an operation.
 *
 * @author Martin Lindström
 */
public class UserCancelException extends BankIDException {

  /** For serializing. */
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /**
   * Default constructor.
   */
  public UserCancelException() {
    super(ErrorCode.USER_CANCEL);
  }

  /**
   * Constructor assigning the message.
   *
   * @param message the message
   */
  public UserCancelException(final String message) {
    super(ErrorCode.USER_CANCEL, message);
  }

  /**
   * Constructor assigning the cause of the error.
   *
   * @param cause cause of the error
   */
  public UserCancelException(final Throwable cause) {
    super(ErrorCode.USER_CANCEL, cause);
  }

  /**
   * Constructor assigning the message and the cause of the error.
   *
   * @param message the message
   * @param cause the cause of the error
   */
  public UserCancelException(final String message, final Throwable cause) {
    super(ErrorCode.USER_CANCEL, message, cause);
  }

}
