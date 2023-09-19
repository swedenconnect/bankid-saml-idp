package se.swedenconnect.bankid.idp.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import se.swedenconnect.bankid.idp.argument.AuthenticatedClientResolver;
import se.swedenconnect.opensaml.sweid.saml2.metadata.entitycategory.EntityCategoryConstants;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"integrationtest"})
@ExtendWith({AuthenticatedClientResolver.class})
public class BankIdIdpIntegrationSetup extends TestContainerSetup {
  public static final TestSp testSp;

  public static final TestSp signSp;

  public static final BankIdApiMock apiMock = new BankIdApiMock();

  @LocalServerPort
  public int port;

  static {
    try {
      testSp = new TestSp(false);
      signSp = new TestSp(true);
      testSp.setWantsAssertionsSigned(true);
      signSp.setWantsAssertionsSigned(true);
      signSp.setEntityCategories(List.of(EntityCategoryConstants.SERVICE_ENTITY_CATEGORY_LOA3_NAME.getUri(),
          EntityCategoryConstants.SERVICE_TYPE_CATEGORY_SIGSERVICE.getUri()));    } catch (Exception e) {
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
}
