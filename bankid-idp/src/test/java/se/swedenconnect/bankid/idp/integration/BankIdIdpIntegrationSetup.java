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
package se.swedenconnect.bankid.idp.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import se.swedenconnect.bankid.idp.argument.AuthenticatedClientResolver;
import se.swedenconnect.opensaml.OpenSAMLInitializer;
import se.swedenconnect.opensaml.OpenSAMLSecurityDefaultsConfig;
import se.swedenconnect.opensaml.OpenSAMLSecurityExtensionConfig;
import se.swedenconnect.opensaml.sweid.saml2.metadata.entitycategory.EntityCategoryConstants;
import se.swedenconnect.opensaml.sweid.xmlsec.config.SwedishEidSecurityConfiguration;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"integrationtest"})
@ExtendWith({AuthenticatedClientResolver.class})
public class BankIdIdpIntegrationSetup extends TestContainerSetup {
  public static final TestSp testSp;

  public static final TestSp signSp;

  public static final BankIdApiMock apiMock = new BankIdApiMock();

  @Autowired
  private RedissonClient client;

  @LocalServerPort
  public int port;

  static {
    try {
      OpenSAMLInitializer.getInstance()
          .initialize(
              new OpenSAMLSecurityDefaultsConfig(new SwedishEidSecurityConfiguration()),
              new OpenSAMLSecurityExtensionConfig());
      testSp = new TestSp(false);
      signSp = new TestSp(true);
      testSp.setWantsAssertionsSigned(true);
      signSp.setWantsAssertionsSigned(true);
      signSp.setEntityCategories(List.of(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA3_NAME.getUri(),
          EntityCategoryConstants.SERVICE_TYPE_CATEGORY_SIGSERVICE.getUri()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("server.servlet.context-path", () -> "/idp");
    // testSp has a method "writeSpMetadata" use if the metadata file needs to be regenerated ...
    //registry.add("saml.idp.metadata-providers[0].location", testSp::getResourcePath);
    registry.add("saml.idp.metadata-providers[0].location", () -> "classpath:/combined-metadata.xml");
  }

  protected void clearSessions() {
    client.getKeys().flushall();
  }
}
