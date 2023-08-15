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

import lombok.AllArgsConstructor;
import se.swedenconnect.bankid.idp.authn.error.UserErrorProperties;

import org.springframework.stereotype.Component;

/**
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@AllArgsConstructor
public class CustomerContactInformationFactory {

  private final UserErrorProperties properties;

  public CustomerContactInformation getContactInformation() {
    if (properties.getShowContactInformation()) {
      return new CustomerContactInformation(properties.getContactEmail(), true);
    }
    return new CustomerContactInformation("", false);
  }
}
