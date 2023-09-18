package se.swedenconnect.bankid.idp.integration.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.IDPSSODescriptorImpl;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import se.swedenconnect.bankid.idp.authn.api.ApiResponse;
import se.swedenconnect.bankid.idp.integration.OpenSamlTestBase;
import se.swedenconnect.bankid.idp.integration.TestSp;
import se.swedenconnect.bankid.idp.integration.TestSpConstants;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGenerator;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext;
import se.swedenconnect.spring.saml.idp.settings.EndpointSettings;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class FrontendClient {
  private final WebClient client;
  private final int port;

  private String session;
  private String xsrfToken;

  private FrontendClient(WebClient client, int port) {
    this.client = client;
    this.port = port;
  }

  public Mono<ApiResponse> poll(boolean qr) {
    Consumer<UriBuilder> customizer = c -> c.queryParam("qr", qr);
    return exchangeApi(withPath(client.post(), "/idp/api/poll", customizer), ApiResponse.class);
  }

  public Mono<Void> cancelApi() {
    WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = client.post();
    return exchangeApi(withPath(requestHeadersUriSpec, "/idp/api/cancel", c -> {}), Void.class);
  }

  @NotNull
  private WebClient.RequestHeadersSpec<?> withPath(WebClient.RequestHeadersUriSpec<?> spec, String path, Consumer<UriBuilder> c) {
    Function<UriBuilder, URI> uriBuilderURIFunction = (uriBuilder) -> {
      uriBuilder.scheme("https");
      uriBuilder.port(8443);
      uriBuilder.host("local.dev.swedenconnect.se");
      uriBuilder.path(path);
      c.accept(uriBuilder);
      return uriBuilder.build();
    };
    WebClient.RequestHeadersSpec<?> uri = spec.uri(uriBuilderURIFunction);
    return withSecurityHeaders(uri);
  }

  @NotNull
  private static <T> Mono<T> exchangeApi(WebClient.RequestHeadersSpec<?> request, Class<T> tClass) {
    return request
        .exchangeToMono(f -> {
          if (f.statusCode().value() == 429) {
            return Mono.error(new CannotAcquireLockException("Resource is busy"));
          }
          if (!f.statusCode().is2xxSuccessful()) {
            return Mono.error(new IllegalStateException("Wrong status code code:" + f.statusCode().value()));
          }
          return f.bodyToMono(tClass);
        });
  }

  @NotNull
  private WebClient.RequestHeadersSpec<?> withSecurityHeaders(WebClient.RequestHeadersSpec<?> uri) {
    return uri
        .cookie("BANKIDSESSION", session)
        .cookie("XSRF-TOKEN", xsrfToken)
        .header("X-XSRF-TOKEN", xsrfToken)
        .header("Access-Control-Allow-Credentials", "true");
  }

  public static FrontendClient init(WebClient client, TestSp testSp, boolean sign) throws Exception {
    final EntityDescriptor spMetadata = testSp.getSpMetadata();
    final EntityDescriptor idpMetadata = getIdpMetadata();
    RoleDescriptor roleDescriptor = idpMetadata.getRoleDescriptors().get(0);
    List<SingleSignOnService> singleSignOnServices = ((IDPSSODescriptorImpl) roleDescriptor).getSingleSignOnServices();
    Optional<SingleSignOnService> first = singleSignOnServices.stream().filter(s -> s.getBinding().contains("HTTP-Redirect")).findFirst();
    first.ifPresent(singleSignOnServices::remove);
    singleSignOnServices.get(0).setLocation(singleSignOnServices.get(0).getLocation().replace(":0", ":8443"));
    MetadataResolver simulatedResolver = TestSpConstants.createMetadataResolver(spMetadata, idpMetadata);
    testSp.setupResponseProcessor(simulatedResolver);

    final AuthnRequestGenerator generator = testSp.createAuthnRequestGenerator(idpMetadata);

    final AuthnRequestGeneratorContext context = SAMLContexts.getContext(sign, idpMetadata.getEntityID());

    MockHttpSession session = new MockHttpSession();
    RequestBuilder request = testSp.generateRequest("https://bankid.swedenconnect.se/idp", generator, context, "s01e01", session, 8443);
    MockHttpServletRequest authRequest = request.buildRequest(Mockito.mock(ServletContext.class));
    String samlRequest = authRequest.getParameter("SAMLRequest");
    SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();
    HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
    WebClient initClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    ClientResponse block = initClient.post()
        .uri(uriBuilder -> {
          uriBuilder.scheme("https");
          uriBuilder.port(8443);
          uriBuilder.host("local.dev.swedenconnect.se");
          uriBuilder.path(authRequest.getRequestURI());
          return uriBuilder.build();
        })
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromFormData("SAMLRequest", samlRequest))
        .exchange()
        .block();
    List<String> cookies = block.headers().header("Set-Cookie");
    Assertions.assertFalse(cookies.isEmpty());
    FrontendClient frontendClient = new FrontendClient(client, 8443);
    Assertions.assertFalse(cookies.isEmpty());
    frontendClient.session = cookies.get(0).split(";")[0].split("=")[1];
    List<String> xsrfCookie = client.get().uri("https://localhost:" + 8443 + "/idp/api/sp")
        .cookie("BANKIDSESSION", frontendClient.session)
        .exchange()
        .block()
        .headers()
        .header("Set-Cookie");
    frontendClient.xsrfToken = xsrfCookie.get(0).split(";")[0].split("=")[1];
    return frontendClient;
  }

  public static EntityDescriptor getIdpMetadata() throws Exception {
    SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();
    HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
    WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    byte[] result = client.get().uri("https://local.dev.swedenconnect.se:8443/idp" + EndpointSettings.SAML_METADATA_PUBLISH_ENDPOINT_DEFAULT).exchange()
        .flatMap(response -> response.bodyToMono(ByteArrayResource.class))
        .map(ByteArrayResource::getByteArray)
        .block();

    return OpenSamlTestBase.unmarshall(
        new ByteArrayInputStream(result), EntityDescriptor.class);
  }

  public String getSession() {
    return session;
  }

  public String complete() {
    List<String> block = client.get()
        .uri("https://local.dev.swedenconnect.se:" + port + "/idp/view/complete")
        .cookie("BANKIDSESSION", session)
        .cookie("XSRF-TOKEN", xsrfToken)
        .header("X-XSRF-TOKEN", xsrfToken)
        .header("Access-Control-Allow-Credentials", "true")
        .exchangeToMono(f -> {
          if (f.statusCode().value() == 302) {
            return Mono.just(f.headers().header("Location"));
          }
          return Mono.error(new RuntimeException("Expected redirect"));
        })
        .log()
        .block();
    Assertions.assertFalse(block.isEmpty());
    return block.get(0);
  }

  public String cancel () {
    List<String> location = client.get()
        .uri("https://local.dev.swedenconnect.se:" + port + "/idp/view/cancel")
        .cookie("BANKIDSESSION", session)
        .cookie("XSRF-TOKEN", xsrfToken)
        .header("X-XSRF-TOKEN", xsrfToken)
        .header("Access-Control-Allow-Credentials", "true")
        .exchangeToMono(f -> {
          if (f.statusCode().value() == 302) {
            return Mono.just(f.headers().header("Location"));
          }
          return Mono.error(new RuntimeException("Expected redirect"));
        })
        .log()
        .block();
    Assertions.assertFalse(location.isEmpty());
    return location.get(0);
  }
}
