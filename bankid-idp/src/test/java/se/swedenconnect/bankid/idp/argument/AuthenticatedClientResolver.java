/*
 * Copyright 2023-2025 Sweden Connect
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
package se.swedenconnect.bankid.idp.argument;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;
import se.swedenconnect.bankid.idp.integration.BankIdIdpIntegrationSetup;
import se.swedenconnect.bankid.idp.integration.client.FrontendClient;

public class AuthenticatedClientResolver implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == FrontendClient.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (SecurityContextHolder.getContext() instanceof AuthenticatedUserSecurityContext ac) {
      return createFrontEndClient(ac.getSign());
    }
    throw new ParameterResolutionException("Failed to resolve AuthenticatedUserClient");
  }

  @NotNull
  public static FrontendClient createFrontEndClient(boolean sign) {
    try {
      SslContext sslContext = SslContextBuilder
          .forClient()
          .trustManager(InsecureTrustManagerFactory.INSTANCE)
          .build();
      HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
      WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
      return FrontendClient.init(client, Map.of(false, BankIdIdpIntegrationSetup.testSp, true, BankIdIdpIntegrationSetup.signSp).get(sign), sign);
    } catch (Exception e) {
      throw new ParameterResolutionException("Failed to resolve AuthenticatedUserClient", e);
    }
  }
}
