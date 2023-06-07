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
package se.swedenconnect.bankid.idp.rp;

/**
 * A {@link RelyingPartyRepository} implementation that is used when the BankID SAML IdP
 * allows any SAML SP to invoke it. This is typically only used for testing.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class TestRelyingPartyRepository implements RelyingPartyRepository {
  
  public TestRelyingPartyRepository() {    
  }

  /** {@inheritDoc} */
  @Override
  public RelyingPartyData getRelyingParty(final String entityId) {
    return null;
  }

}
