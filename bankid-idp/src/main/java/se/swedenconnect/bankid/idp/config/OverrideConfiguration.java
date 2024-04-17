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
package se.swedenconnect.bankid.idp.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.swedenconnect.bankid.idp.authn.api.overrides.ContentOverride;
import se.swedenconnect.bankid.idp.authn.api.overrides.CssOverride;
import se.swedenconnect.bankid.idp.authn.api.overrides.MessageOverride;
import se.swedenconnect.bankid.idp.authn.api.overrides.OverrideFileLoader;
import se.swedenconnect.bankid.idp.authn.api.overrides.OverrideService;

/**
 * Configurations for front-end UI overrides.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 * @author Mattias Kesti
 */
@Configuration
public class OverrideConfiguration {

  private final OverrideProperties overrideProperties;

  /**
   * Constructor.
   *
   * @param properties BankID configuration properties
   */
  public OverrideConfiguration(final BankIdConfigurationProperties properties) {
    this.overrideProperties = Optional.ofNullable(properties.getUi().getOverride())
        .orElseGet(() -> new OverrideProperties());
  }

  @Bean
  OverrideService overrideService(final OverrideFileLoader fileLoader,
      final List<Supplier<CssOverride>> cssOverrides,
      final List<Supplier<MessageOverride>> messageOverrides,
      final List<Supplier<ContentOverride>> contentOverrides,
      final BankIdConfigurationProperties properties) {
    return new OverrideService(cssOverrides, messageOverrides, contentOverrides,properties.getUi().getOverride(), fileLoader);
  }

  @Bean
  OverrideFileLoader overrideFileLoader(final ObjectMapper mapper) {
    return new OverrideFileLoader(this.overrideProperties, mapper);
  }

}
