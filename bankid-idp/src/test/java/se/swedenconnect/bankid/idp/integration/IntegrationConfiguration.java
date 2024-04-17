/*
 * Copyright 2023-2024 Sweden Connect
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
package se.swedenconnect.bankid.idp.integration;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;

@Configuration
public class IntegrationConfiguration {

  @Bean
  @Primary
  public Function<BankIdConfigurationProperties.RelyingPartyConfiguration, WebClient> testWebClientFactory() {
    return rp -> {
      return WebClient.builder().baseUrl("http://localhost:9000").build();
    };
  }
}
