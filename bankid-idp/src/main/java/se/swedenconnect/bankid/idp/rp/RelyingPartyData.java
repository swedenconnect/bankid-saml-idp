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
import java.util.Objects;
import java.util.Optional;

import se.swedenconnect.bankid.idp.authn.DisplayText;
import se.swedenconnect.bankid.idp.config.BankIdRequirement;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;

/**
 * The data associated to a BankID relying party.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class RelyingPartyData {

  /**
   * The SAML entityID:s for the relying party.
   */
  private List<String> entityIds;

  /**
   * The BankID client that contains the RP-certificate for the client.
   */
  private final BankIDClient client;

  /** The text to display when authenticating. May be {@code null}. */
  private final DisplayText loginText;

  /** The text to display when signing if a {@code SignMessage} is not received. */
  private final DisplayText fallbackSignText;

  /** The BankID requirements (may be {@code null}). */
  private final BankIdRequirement requirement;

  /**
   * The UI info for a Relying Party is normally extracted from the SAML metadata, but there are cases where you may
   * want to manually configure these data elements (for example if the metadata does not contain this information, or
   * you simply want to override it). This element holds this information.
   */
  private final RelyingPartyUiInfo uiInfo;

  /**
   * Constructor.
   *
   * @param client the BankID client
   * @param entityIds the SAML entityID:s for this RP
   * @param loginText the login text to use (may be {@code null})
   * @param fallbackSignText the fallback text for signature messages (must be set)
   * @param uiInfo optional UI info
   * @param requirement optional specific BankID requirements
   */
  public RelyingPartyData(final BankIDClient client, final List<String> entityIds,
      final DisplayText loginText, final DisplayText fallbackSignText,
      final RelyingPartyUiInfo uiInfo, final BankIdRequirement requirement) {
    this.client = Objects.requireNonNull(client, "client must not be null");
    this.entityIds = Optional.ofNullable(entityIds)
        .map(Collections::unmodifiableList)
        .orElseGet(Collections::emptyList);
    this.loginText = loginText;
    this.fallbackSignText = Objects.requireNonNull(fallbackSignText, "fallbackSignText must not be null");
    this.uiInfo = uiInfo;
    this.requirement = requirement;
  }

  /**
   * Gets the ID for this Relying Party.
   *
   * @return the ID
   */
  public String getId() {
    return this.client.getIdentifier();
  }

  /**
   * Gets the BankID client for this Relying Party.
   *
   * @return a {@link BankIDClient}
   */
  public BankIDClient getClient() {
    return this.client;
  }

  /**
   * Gets a list of all SAML entityID:s (SP:s) that this Relying Party serves.
   * <p>
   * If the list is empty and the IdP is in test mode, this means that all SP:s are served by this RP.
   * </p>
   *
   * @return a list of entityID:s
   */
  public List<String> getEntityIds() {
    return this.entityIds;
  }

  /**
   * Gets text to display when authenticating.
   *
   * @return the text or {@code null}
   */
  public DisplayText getLoginText() {
    return this.loginText;
  }

  /**
   * Gets the text to display when signing if a {@code SignMessage} is not received.
   *
   * @return the sign text
   */
  public DisplayText getFallbackSignText() {
    return this.fallbackSignText;
  }

  /**
   * Gets the {@link RelyingPartyUiInfo} if present
   *
   * @return the {@link RelyingPartyUiInfo} or {@code null}
   */
  public RelyingPartyUiInfo getUiInfo() {
    return this.uiInfo;
  }

  /**
   * Gets the BankID requirements for the relaying party.
   *
   * @return the requirements
   */
  public BankIdRequirement getRequirement() {
    return this.requirement;
  }

  /**
   * Predicate that tells whether the supplied SAML entityID is served by this RP.
   *
   * @param entityId the SAML SP entityID
   * @return {@code true} if this RP serves this SP and {@code false} otherwise
   */
  public boolean matches(final String entityId) {
    return this.entityIds.isEmpty() ? true : this.entityIds.contains(entityId);
  }

}
