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
package se.swedenconnect.bankid.idp.authn;

import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;

import java.io.IOException;

/**
 * Controller for templating index.html for setting base path.
 *
 * @author Felix Hellman
 */
@Configuration
public class IndexRouteConfiguration {

  @Bean
  RouterFunction<ServerResponse> getIndex(
      final ServletContext context,
      final BankIdConfigurationProperties properties
  ) {
    final String authnPath = properties.getAuthn().getAuthnPath();
    return RouterFunctions
        .route()
        .GET(authnPath + "/", getServerResponseHandlerFunction(context, authnPath)).build();
  }

  @Bean
  RouterFunction<ServerResponse> getBaseIndex(
      final ServletContext context,
      final BankIdConfigurationProperties properties
  ) {
    final String authnPath = properties.getAuthn().getAuthnPath();
    return RouterFunctions
        .route()
        .GET(authnPath, getServerResponseHandlerFunction(context, authnPath)).build();
  }

  private HandlerFunction<ServerResponse> getServerResponseHandlerFunction(final ServletContext context, final String authnPath) {
    return request -> {
      return ServerResponse.ok()
          .contentType(MediaType.TEXT_HTML)
          .body(baseIndexTemplate(
              context.getContextPath(),
              "%s%s".formatted(context.getContextPath(), authnPath)
          ));
    };
  }

  private static final String ELEMENT = """
      <base id="base-href-id" href="/"/>
      """;

  private static final String REPLACE_ELEMENT_FORMAT = """
      <base id="base-href-id" href="%s"/>
      """;

  private static final String ROUTER_ELEMENT = """
      <p id="router-href-id" href="/" hidden="true"/>
      """;

  private static final String REPLACE_ROUTER_ELEMENT_FORMAT = """
      <p id="router-href-id" href="%s" hidden="true"/>
      """;

  /**
   * @param contextPath to template into index.html
   * @return modified index.html with basepath set to context-path if context-path is set.
   * @throws IOException if index.html is not found
   */
  public String baseIndexTemplate(final String contextPath, final String routerPath) throws IOException {
    final byte[] bytes = new ClassPathResource("static/index.html").getInputStream().readAllBytes();
    final String file = new String(bytes);
    final StringBuilder context = new StringBuilder(contextPath);
    if (!contextPath.endsWith("/")) {
      context.append("/");
    }
    final StringBuilder router = new StringBuilder(routerPath);
    if (!routerPath.endsWith("/")) {
      router.append("/");
    }
    return file
        .replace(ELEMENT, REPLACE_ELEMENT_FORMAT.formatted(context.toString()))
        .replace(ROUTER_ELEMENT, REPLACE_ROUTER_ELEMENT_FORMAT.formatted(router.toString()));
  }
}

