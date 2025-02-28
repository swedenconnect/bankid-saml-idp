/*
 *
 *  * Copyright 2023-${year} Sweden Connect
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package se.swedenconnect.bankid.idp.authn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import se.swedenconnect.bankid.idp.config.BankIdConfiguration;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;

import java.net.URI;
import java.util.Arrays;

/**
 * The {@code ForwardRouter} class is responsible for configuring routing
 * frontend routes to the Vue 3 router served by {@code index.html}.
 *
 * @author Felix Hellman
 */

@Configuration
public class ForwardRouter {

  private final URI forward;
  private final BankIdConfigurationProperties properties;

  public ForwardRouter(final BankIdConfigurationProperties properties) {
    this.forward = URI.create("forward:" + properties.getAuthn().getAuthnPath());
    this.properties = properties;
  }

  @Bean
  RouterFunction<ServerResponse> getFrontendForwardingRoutes() {
    final RouterFunctions.Builder builder = RouterFunctions.route();

    Arrays.stream(BankIdConfiguration.frontendRoutes(this.properties.getAuthn().getAuthnPath()))
        .filter(route -> !route.equals("/"))
        .forEach(route ->
            builder.GET(route, (request -> {
              return ServerResponse.status(302).location(forward).build();
            })));

    return builder.build();
  }
}
