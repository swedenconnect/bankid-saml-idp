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
package se.swedenconnect.bankid.idp.authn.api;

import org.springframework.stereotype.Component;
import se.swedenconnect.bankid.idp.config.UiProperties.UserErrorProperties;

import java.util.Objects;

/**
 * Supplies customer information.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
public class CustomerContactInformationFactory {

  private final UserErrorProperties properties;

  /**
   * Constructor.
   *
   * @param properties the user error properties
   */
  public CustomerContactInformationFactory(final UserErrorProperties properties) {
    this.properties = Objects.requireNonNull(properties, "properties must not be null");
  }

  public CustomerContactInformation getContactInformation() {
    if (this.properties.isShowContactInformation()) {
      return new CustomerContactInformation(this.properties.getContactEmail(), true);
    }
    return new CustomerContactInformation("", false);
  }
}
