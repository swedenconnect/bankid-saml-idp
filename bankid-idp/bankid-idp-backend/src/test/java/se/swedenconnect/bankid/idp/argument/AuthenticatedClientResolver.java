package se.swedenconnect.bankid.idp.argument;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import se.swedenconnect.bankid.idp.integration.BankIdFrontendClient;
import se.swedenconnect.bankid.idp.integration.BankIdIdpIntegrationSetup;

public class AuthenticatedClientResolver implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == BankIdFrontendClient.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (SecurityContextHolder.getContext() instanceof AuthenticatedUserSecurityContext ac) {
      try {
        SslContext sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        return BankIdFrontendClient.init(client, BankIdIdpIntegrationSetup.testSp);
      } catch (Exception e) {
        throw new ParameterResolutionException("Failed to resolve AuthenticatedUserClient", e);
      }
    }
    throw new ParameterResolutionException("Failed to resolve AuthenticatedUserClient");
  }
}
