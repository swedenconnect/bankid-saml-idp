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
package se.swedenconnect.bankid.idp.rp;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository for relying parties.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class DefaultRelyingPartyRepository implements RelyingPartyRepository {

  /**
   * The configured relying parties.
   */
  private final List<RelyingPartyData> relyingParties;

  /**
   * Constructor.
   *
   * @param relyingParties the relying parties
   */
  public DefaultRelyingPartyRepository(final List<RelyingPartyData> relyingParties) {
    this.relyingParties = Optional.ofNullable(relyingParties)
        .map(Collections::unmodifiableList)
        .orElseThrow(() -> new IllegalArgumentException("relyingParties must not be null"));
  }

  /** {@inheritDoc} */
  @Override
  public RelyingPartyData getRelyingParty(final String entityId) {
    return this.relyingParties.stream()
        .filter(rp -> rp.matches(entityId))
        .findFirst()
        .orElse(null);
  }

}
