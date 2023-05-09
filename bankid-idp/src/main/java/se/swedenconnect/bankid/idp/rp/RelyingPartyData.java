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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;

/**
 * The data associated to a BankID relying party.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelyingPartyData {
  
  /**
   * The SAML entityID for the relying party.
   */
  private String entityId;
  
  /**
   * The BankID client that contains the RP-certificate for the client.
   */
  private BankIDClient client;
  
  // TODO: custom display texts, custom logo ...

}
