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
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.rpapi.types.BankIDException;

/**
 * Exception for expired BankID sessions.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdSessionExpiredException extends BankIDException {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  private final PollRequest request;

  /**
   * Constructor.
   * 
   * @param request the {@link PollRequest}
   */
  public BankIdSessionExpiredException(final PollRequest request) {
    super("The session towards BankID has timed out");
    this.request = request;
  }

  /**
   * Gets the {@link PollRequest} that were active when the session timed out.
   * 
   * @return a {@link PollRequest}
   */
  public PollRequest getRequest() {
    return this.request;
  }

}
