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

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import se.swedenconnect.bankid.idp.ApplicationVersion;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

/**
 * An {@link Authentication} object representing a successful BankID operation.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdAuthenticationToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = ApplicationVersion.SERIAL_VERSION_UID;

  /**
   * Constructor.
   * 
   * @param completionData the {@link CompletionData} received from the BankID server
   */
  public BankIdAuthenticationToken(final CompletionData completionData) {
    super(Collections.emptyList());
    this.setDetails(completionData);
    this.setAuthenticated(true);
  }

  /**
   * Returns {@code null}.
   */
  @Override
  public Object getCredentials() {
    return null;
  }

  /**
   * Returns a {@link CompletionData} received from the BankID server.
   */
  @Override
  public Object getPrincipal() {
    return this.getDetails();
  }

}
