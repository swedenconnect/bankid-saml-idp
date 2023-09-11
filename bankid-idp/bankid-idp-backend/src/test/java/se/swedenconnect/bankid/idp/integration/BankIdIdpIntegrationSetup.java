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

  @LocalServerPort
  public int port;

  static {
    WireMockServer wireMockServer = new WireMockServer(9000);
    wireMockServer.start();
    String response = """
        {
          "orderRef": "%s",
          "autoStartToken": "%s",
          "qrStartToken": "%s",
          "qrStartSecret": "%s",
          "status": "%s"
        }
        """.formatted(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        "PENDING"
    );
    wireMockServer.stubFor(post("/auth").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    wireMockServer.stubFor(post("/collect").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    wireMockServer.stubFor(post("/sign").willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));
    wireMockServer.stubFor(post("/cancel").willReturn(aResponse()));
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
