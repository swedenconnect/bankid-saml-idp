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
package se.swedenconnect.bankid.idp.config.session;

import java.util.Objects;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.spring.saml.idp.autoconfigure.settings.IdentityProviderConfigurationProperties;

/**
 * Configuration class that is responsible of mapping the deprecated session configuration to the "new" way of
 * configuring sessions.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@EnableConfigurationProperties({ BankIdConfigurationProperties.class, IdentityProviderConfigurationProperties.class })
@Slf4j
public class SessionDeprecationConfiguration {

  /** The BankID properties. */
  private final BankIdConfigurationProperties bankidProperties;

  /** The IdP properties. */
  private final IdentityProviderConfigurationProperties idpProperties;

  /**
   * Constructor.
   *
   * @param bankidProperties the BankID properties
   * @param idpProperties the IdP properties
   */
  public SessionDeprecationConfiguration(final BankIdConfigurationProperties bankidProperties,
      final IdentityProviderConfigurationProperties idpProperties) {
    this.bankidProperties = bankidProperties;
    this.idpProperties = idpProperties;
  }

  @Bean
  SessionPropertyModifier sessionProperyModifier() {
    return new SessionPropertyModifier(this.bankidProperties, this.idpProperties);
  }

  /**
   * Modifies the settings for {@link IdentityProviderConfigurationProperties} based on the deprecated settings of
   * {@link BankIdConfigurationProperties}.
   */
  class SessionPropertyModifier {

    public SessionPropertyModifier(final BankIdConfigurationProperties bankidProperties,
        final IdentityProviderConfigurationProperties idpProperties) {

      if (idpProperties.getSession().getModule() == null) {
        idpProperties.getSession().setModule(bankidProperties.getSession().getModule());
        log.warn("DEPRECATION WARNING. Mapping bankid.session.module={} to saml.idp.session.module={}",
            bankidProperties.getSession().getModule(), bankidProperties.getSession().getModule());
      }
      else if (bankidProperties.getSession().getModule() != null &&
          !Objects.equals(idpProperties.getSession().getModule(), bankidProperties.getSession().getModule())) {
        throw new BeanCreationException(
            "Invalid configuration bankid.session.module and saml.idp.session.module can not be set to different values");
      }

      if (idpProperties.getReplay().getType() == null) {
        idpProperties.getReplay().setType(bankidProperties.getSession().getModule());
        log.info("Setting saml.idp.replay.type={} based on bankid.session.module setting",
            bankidProperties.getSession().getModule());
      }
    }

  }

}
