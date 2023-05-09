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

import java.util.List;
import java.util.Objects;

/**
 * Repository for relying parties.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RelyingPartyRepository {

  /** The configured relying parties. */
  private final List<RelyingPartyData> relyingParties;

  /**
   * Constructor.
   * 
   * @param relyingParties the relying parties
   */
  public RelyingPartyRepository(final List<RelyingPartyData> relyingParties) {
    this.relyingParties = Objects.requireNonNull(relyingParties, "relyingParties must not be null");
  }

  /**
   * Based on a SAML entityID we return the {@link RelyingPartyData} associated with this ID.
   * 
   * @param entityId the SAML entityID for the RP
   * @return a {@link RelyingPartyData} or {@code null} if not present
   */
  public RelyingPartyData getRelyingParty(final String entityId) {
    return this.relyingParties.stream()
        .filter(r -> Objects.equals(entityId, r.getEntityId()))
        .findFirst()
        .orElse(null);
  }

}
