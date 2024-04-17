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
package se.swedenconnect.bankid.idp.authn.error;

import se.swedenconnect.bankid.idp.ApplicationVersion;

/**
 * Exception class for validation errors in BankID data received from the BankID server.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdValidationException extends BankIdTraceableException {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  /**
   * Constructor.
   * 
   * @param orderRef the order reference
   * @param msg the error message
   */  
  public BankIdValidationException(final String orderRef, final String msg) {
    super(orderRef, msg);
  }

  /**
   * Constructor.
   * 
   * @param orderRef the order reference
   * @param msg the error message
   * @param cause the cause of the error
   */  
  public BankIdValidationException(final String orderRef, final String msg, final Throwable cause) {
    super(orderRef, msg, cause);
  }

}
