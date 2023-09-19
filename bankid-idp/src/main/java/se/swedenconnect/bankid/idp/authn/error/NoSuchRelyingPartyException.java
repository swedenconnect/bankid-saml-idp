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
import se.swedenconnect.bankid.rpapi.types.BankIDException;

/**
 * Runtime exception for cases where the flow is invoked for a non-registered RP.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class NoSuchRelyingPartyException extends BankIDException {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  private final String entityId;

  /**
   * Constructor.
   *
   * @param entityId the entityID for the SP that does not exist
   */
  public NoSuchRelyingPartyException(final String entityId) {
    super("Not registered SP - " + entityId);
    this.entityId = entityId;
  }

  /**
   * The entityID for the requester that was not registered.
   * 
   * @return the entityID
   */
  public String getEntityId() {
    return this.entityId;
  }
}
