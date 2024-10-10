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
package se.swedenconnect.bankid.rpapi.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.Requirement;

/**
 * Representation of the parameters required for an authenticate call.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class AuthenticateRequest {

  /** The The user IP address as seen by RP. */
  private final String endUserIp;

  /**
   * Text displayed to the user during authentication with BankID, with the purpose of providing context for the
   * authentication and to enable users to detect identification errors and averting fraud attempts.
   */
  private final UserVisibleData userVisibleData;

  /** Requirements on how the auth order must be performed. */
  private final Requirement requirement;

  /** The returnUrl given when starting the app on the same device. */
  private final String returnUrl;

  /** The nonce given when starting the app on the same device. */
  private final String nonce;

  /**
   * Creates parameter object for an {@link BankIDClient#authenticate(AuthenticateRequest)} call.
   *
   * @param endUserIp the user IP address as seen by the relying party
   * @param userVisibleData data to display to the user during authentication (optional)
   * @param requirement used by the relying party to set requirements how the authentication operation must be
   *     performed. Default rules are applied if omitted
   * @throws BankIDException for errors
   */
  /*
  public AuthenticateRequest(@Nonnull final String endUserIp, @Nullable final UserVisibleData userVisibleData,
      @Nullable final Requirement requirement) {
    this(endUserIp, userVisibleData, requirement, null, null);
  }
   */

  /**
   * Creates parameter object for an {@link BankIDClient#authenticate(AuthenticateRequest)} call.
   *
   * @param endUserIp the user IP address as seen by the relying party
   * @param userVisibleData data to display to the user during authentication (optional)
   * @param requirement used by the relying party to set requirements how the authentication operation must be
   *     performed. Default rules are applied if omitted
   * @param returnUrl the returnUrl given when starting the app on the same device
   * @param nonce the nonce given when starting the app on the same device
   * @throws BankIDException for errors
   */
  public AuthenticateRequest(@Nonnull final String endUserIp, @Nullable final UserVisibleData userVisibleData,
      @Nullable final Requirement requirement, @Nullable final String returnUrl, @Nullable final String nonce) {
    this.endUserIp = endUserIp;
    this.userVisibleData = userVisibleData;
    this.requirement = requirement;
    this.returnUrl = returnUrl;
    this.nonce = nonce;
  }

  /**
   * Gets the user IP address as seen by the relying party.
   *
   * @return the user IP
   */
  @Nonnull
  public String getEndUserIp() {
    return this.endUserIp;
  }

  /**
   * Gets the data to display to the user during authentication.
   *
   * @return the data to display or {@code null}
   */
  @Nullable
  public UserVisibleData getUserVisibleData() {
    return this.userVisibleData;
  }

  /**
   * Gets the authentication requirements.
   *
   * @return the {@link Requirement} or {@code null}
   */
  @Nullable
  public Requirement getRequirement() {
    return this.requirement;
  }

  /**
   * Gets the returnUrl given when starting the app on the same device.
   *
   * @return the return URL or {@code null}
   */
  @Nullable
  public String getReturnUrl() {
    return this.returnUrl;
  }

  /**
   * Gets the nonce given when starting the app on the same device.
   *
   * @return the nonce or {@code null}
   */
  @Nullable
  public String getNonce() {
    return this.nonce;
  }
}
