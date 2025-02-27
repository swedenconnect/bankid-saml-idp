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
package se.swedenconnect.bankid.idp.config;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.rpapi.types.Requirement;

/**
 * Configuration for specific RP BankID requirements.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankIdRequirement {

  /**
   * Tells whether users are required to use their PIN code during BankID authentication, even if they have biometrics
   * activated.
   */
  private boolean pinCodeAuth = false;

  /**
   * Tells whether users are required to use their PIN code during BankID signing, even if they have biometrics
   * activated.
   */
  private boolean pinCodeSign = true;

  /**
   * If present, and set to "true", the client needs to provide MRTD (Machine readable travel document) information to
   * complete the order. Only Swedish passports and national ID cards are supported.
   */
  private Boolean mrtd;

  /**
   * Object identifiers for which policies that should be used.
   */
  private List<String> certificatePolicies;

  /**
   * Requirement for which type of smart card reader that is required.
   */
  private Requirement.CardReaderRequirement cardReader;

}
