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
 * Representation of the parameters required for a sign call.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class SignatureRequest extends AuthenticateRequest {

  /**
   * Creates parameter object for an {@link BankIDClient#sign(SignatureRequest)} call.
   *
   * @param endUserIp the user IP address as seen by the relying party
   * @param dataToSign the data to sign
   * @param requirement used by the relying party to set requirements how the sign operation must be performed.
   *     Default rules are applied if omitted
   * @throws BankIDException for errors
   */
  /*
  public SignatureRequest(@Nonnull final String endUserIp, @Nonnull final DataToSign dataToSign,
      @Nullable final Requirement requirement) {
    super(endUserIp, dataToSign, requirement);
  }
   */

  /**
   * Creates parameter object for an {@link BankIDClient#sign(SignatureRequest)} call.
   *
   * @param endUserIp the user IP address as seen by the relying party
   * @param dataToSign the data to sign
   * @param requirement used by the relying party to set requirements how the sign operation must be performed.
   *     Default rules are applied if omitted
   * @param returnUrl the returnUrl given when starting the app on the same device
   * @param nonce the nonce given when starting the app on the same device
   * @throws BankIDException for errors
   */
  public SignatureRequest(@Nonnull final String endUserIp, @Nonnull final DataToSign dataToSign,
      @Nullable final Requirement requirement, @Nullable final String returnUrl, @Nullable final String nonce) {
    super(endUserIp, dataToSign, requirement, returnUrl, nonce);
  }

  /**
   * Gets the data to sign.
   *
   * @return the data to sign
   */
  public DataToSign getDataToSign() {
    return (DataToSign) this.getUserVisibleData();
  }

}
