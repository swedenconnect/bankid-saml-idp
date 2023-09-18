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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import se.swedenconnect.bankid.idp.authn.api.CustomerContactInformation;
import se.swedenconnect.bankid.idp.authn.api.CustomerContactInformationFactory;

class CustomerContactInformationFactoryTest {

  @Test
  void customerContactInfoHiddenWhenConfigured() {
    CustomerContactInformationFactory customerContactInformationFactory =
        new CustomerContactInformationFactory(UserErrorPropertiesFixture.EMPTY_PROPERTIES);
    CustomerContactInformation contactInformation = customerContactInformationFactory.getContactInformation();
    Assertions.assertEquals("", contactInformation.getEmail());
    Assertions.assertEquals(false, contactInformation.getDisplayInformation());
  }

  @Test
  void customerContactInfoShowWhenConfigured() {
    CustomerContactInformationFactory customerContactInformationFactory =
        new CustomerContactInformationFactory(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE);
    CustomerContactInformation contactInformation = customerContactInformationFactory.getContactInformation();
    Assertions.assertEquals(UserErrorPropertiesFixture.SHOW_EMAIL_NO_TRACE.getContactEmail(),
        contactInformation.getEmail());
    Assertions.assertEquals(true, contactInformation.getDisplayInformation());
  }

}