package se.swedenconnect.bankid.idp.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import se.swedenconnect.bankid.idp.argument.AuthenticatedClientResolver;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"local", "integrationtest"})
@ExtendWith({AuthenticatedClientResolver.class})
public class BankIdIdpIntegrationSetup extends TestContainerSetup {
  public static final TestSp testSp;

  public static final BankIdApiMock apiMock = new BankIdApiMock();

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
