package se.swedenconnect.bankid.idp.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.nio.file.Files;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"local"})
public class BankIdIdpIntegrationSetup extends TestContainerSetup {
  protected static final TestSp testSp;

  @LocalServerPort
  public int port;

  static {
    try {
      testSp = new TestSp();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("server.servlet.context-path", () -> "/idp");
    // testSp has a method "writeSpMetadata" use if the metadata file needs to be regenerated ...
    registry.add("saml.idp.metadata-providers[0].location", testSp::getResourcePath);
  }
}
