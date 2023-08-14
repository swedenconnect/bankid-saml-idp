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
package se.swedenconnect.bankid.idp.authn;

import lombok.Getter;
import se.swedenconnect.bankid.idp.ApplicationVersion;

/**
 * Exception class for BankID authentication errors.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdAuthenticationException extends RuntimeException {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  @Getter
  private final String identifier;

  /**
   * Constructor.
   *
   * @param identifier the ID of the operation
   */
  public BankIdAuthenticationException(final String identifier) {
    super();
    this.identifier = identifier;
  }
}
