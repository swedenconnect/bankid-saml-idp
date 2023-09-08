package se.swedenconnect.bankid.idp.integration;


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.IDPSSODescriptorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.authn.api.BankIdApiController;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGenerator;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;
import se.swedenconnect.spring.saml.idp.settings.EndpointSettings;

import javax.net.ssl.SSLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class BankIdIdpIT extends BankIdIdpIntegrationSetup {

  @Autowired
  private BankIdApiController controller;

  @Autowired
  private MetadataResolver internalResolver;
  private MetadataResolver simulatedResolver;

  @Test
  void emptyRequestContext_WillFail() {
    HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(servletRequest.getSession()).thenReturn(Mockito.mock(HttpSession.class));
    Assertions.assertThrows(UnrecoverableSaml2IdpException.class, () -> controller.poll(servletRequest, false).block());
  }

  @Test
  void resolveLocalMetadata() throws Exception {
    // POST https://eid.idsec.se/testmyeid/saml2/request
    // skapa AuthNRequest
    final EntityDescriptor spMetadata = testSp.getSpMetadata();
    final EntityDescriptor idpMetadata = this.getIdpMetadata();
    RoleDescriptor roleDescriptor = idpMetadata.getRoleDescriptors().get(0);
    List<SingleSignOnService> singleSignOnServices = ((IDPSSODescriptorImpl) roleDescriptor).getSingleSignOnServices();
    Optional<SingleSignOnService> first = singleSignOnServices.stream().filter(s -> s.getBinding().contains("HTTP-Redirect")).findFirst();
    first.ifPresent(singleSignOnServices::remove);
    singleSignOnServices.get(0).setLocation(singleSignOnServices.get(0).getLocation().replace(":0", ":" + port));
    this.simulatedResolver = TestSpConstants.createMetadataResolver(spMetadata, idpMetadata);
    testSp.setupResponseProcessor(this.simulatedResolver);

    final AuthnRequestGenerator generator = testSp.createAuthnRequestGenerator(idpMetadata);

    final AuthnRequestGeneratorContext context = new AuthnRequestGeneratorContext() {

      @Override
      public String getPreferredBinding() {
        return SAMLConstants.SAML2_POST_BINDING_URI;
      }
    };
    MockHttpSession session = new MockHttpSession();
    RequestBuilder request = testSp.generateRequest("https://bankid.swedenconnect.se/idp/local", generator, context, "s01e01", session, port);
    MockHttpServletRequest authRequest = request.buildRequest(Mockito.mock(ServletContext.class));
    String samlRequest = authRequest.getParameter("SAMLRequest");
    String relayState = authRequest.getParameter("RelayState");
    SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();
    HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
    WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    ClientResponse block = client.post()
        .uri(uriBuilder -> {
          uriBuilder.scheme("https");
          uriBuilder.port(port);
          uriBuilder.host("local.dev.swedenconnect.se");
          uriBuilder.path(authRequest.getRequestURI());
          return uriBuilder.build();
        })
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromFormData("SAMLRequest", samlRequest))
        .exchange()
        .block();
    List<String> setCookie = block.headers().header("Set-Cookie");
    Assertions.assertFalse(setCookie.isEmpty());

    String bankidSession = setCookie.get(0).split(";")[0];
    WebClient authenticatedClient = createClientWithHeaders(setCookie.get(0));
    List<String> xsrfCookie = authenticatedClient.get().uri("https://localhost:" + port + "/idp/api/sp")
        .exchange()
        .block()
        .headers().header("Set-Cookie");
    String xsrf = xsrfCookie.get(0).split(";")[0];
    ApiResponse apiResponse = authenticatedClient.post()
        .uri("https://local.dev.swedenconnect.se:" + port + "/idp/api/poll")
        .cookie("BANKIDSESSION", bankidSession.split("=")[1])
        .cookie("XSRF-TOKEN", xsrf.split("=")[1])
        .header("X-XSRF-TOKEN", xsrf.split("=")[1])
        .header("Access-Control-Allow-Credentials", "true")
        .exchangeToMono(f -> {
          if (!f.statusCode().is2xxSuccessful()) {
            return Mono.error(new IllegalStateException("Wrong status code code:" + f.statusCode().value()));
          }
          return f.bodyToMono(ApiResponse.class);
        })
        .log()
        .block();

    Assertions.assertNotNull(apiResponse);
    Assertions.assertNotNull(apiResponse.getAutoStartToken());
  }

  private WebClient createClientWithHeaders(String session) throws SSLException {
    String[] split = session.split(";");
    SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();
    HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
    httpClient.cookie("BANKIDSESSION", c -> {
      c.setValue(split[0].split("=")[1]);
      c.setPath(split[1]);
      c.setSecure(true);
      c.setHttpOnly(true);
    });
    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

  private EntityDescriptor getIdpMetadata() throws Exception {
    SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();
    HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
    WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    byte[] result = client.get().uri("https://local.dev.swedenconnect.se:" + port + "/idp" + EndpointSettings.SAML_METADATA_PUBLISH_ENDPOINT_DEFAULT).exchange()
        .flatMap(response -> response.bodyToMono(ByteArrayResource.class))
        .map(ByteArrayResource::getByteArray)
        .block();

    return OpenSamlTestBase.unmarshall(
        new ByteArrayInputStream(result), EntityDescriptor.class);
  }
}
