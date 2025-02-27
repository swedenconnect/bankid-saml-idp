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
package se.swedenconnect.bankid.rpapi.service;

import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.Requirement;

/**
 * Representation of the parameters required for a sign call.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class SignatureRequest {

  /** The The user IP address as seen by RP. */
  private final String endUserIp;

  /** The data to sign (and display). */
  private final DataToSign dataToSign;

  /** Requirements on how the sign order must be performed. */
  private final Requirement requirement;

  /**
   * Creates parameter object for an {@link BankIDClient#sign(SignatureRequest)} call.
   *
   * @param endUserIp the user IP address as seen by the relying party
   * @param dataToSign the data to sign
   * @param requirement used by the relying party to set requirements how the sign operation must be performed. Default
   *          rules are applied if omitted
   * @throws BankIDException for errors
   */
  public SignatureRequest(final String endUserIp, final DataToSign dataToSign, final Requirement requirement) {
    this.endUserIp = endUserIp;
    this.dataToSign = dataToSign;
    this.requirement = requirement;
  }

  /**
   * Gets the user IP address as seen by the relying party.
   *
   * @return the user IP
   */
  public String getEndUserIp() {
    return this.endUserIp;
  }

  /**
   * Gets the data to sign.
   *
   * @return the data to sign
   */
  public DataToSign getDataToSign() {
    return this.dataToSign;
  }

  /**
   * Gets the signing requirements.
   *
   * @return the {@link Requirement} or {@code null}
   */
  public Requirement getRequirement() {
    return this.requirement;
  }

}
