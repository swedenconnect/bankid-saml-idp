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
import reactor.netty.http.client.HttpClient;
import se.swedenconnect.bankid.idp.authn.api.BankIdApiController;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGenerator;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext;
import se.swedenconnect.spring.saml.idp.error.UnrecoverableSaml2IdpException;
import se.swedenconnect.spring.saml.idp.settings.EndpointSettings;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.util.List;
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
        .header("X-FORWARDED-PORT", "0")
        .header("X-FORWARDED-FOR", "127.0.0.1")
        .header("X-FORWARDED-PROTO", "https")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromFormData("SAMLRequest", samlRequest))
        .exchange()
        .block();
    System.out.println(block.statusCode().toString());
    System.out.println(block.headers().asHttpHeaders().toString());

    // GET https://sandbox.swedenconnect.se/bankid/idp/saml2/redirect/authn?SAMLRequest=
    // Vi tar emot SAML request
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
