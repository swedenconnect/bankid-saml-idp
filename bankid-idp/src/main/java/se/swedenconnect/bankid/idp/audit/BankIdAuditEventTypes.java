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
package se.swedenconnect.bankid.idp.audit;

/**
 * Audit event types for the BankID IdP.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public enum BankIdAuditEventTypes {

  /** Received request for auth/sign. */
  BANKID_RECEIVED_REQUEST("BANKID_RECEIVED_REQUEST"),

  /** The BankID operation has been initiated, i.e., the underlying BankID server has been invoked. */
  INIT("BANKID_INIT"),

  /** A BankID authentication operation has been successfully completed. */
  AUTH_COMPLETE("BANKID_AUTH_COMPLETE"),

  /** A BankID signature operation has been successfully completed. */
  SIGN_COMPLETE("BANKID_SIGN_COMPLETE"),

  /** An operation that was started was cancelled by the user. */
  BANKID_CANCEL("BANKID_CANCEL"),

  /** An error occurred when processing a BankID request. */
  BANKID_ERROR("BANKID_ERROR");

  /** The event type name. */
  private final String typeName;

  /**
   * Constructor.
   *
   * @param typeName the event type name
   */
  BankIdAuditEventTypes(final String typeName) {
    this.typeName = typeName;
  }

  /**
   * Gets the event type name.
   * @return the event type name
   */
  public String getTypeName() {
    return this.typeName;
  }

}
