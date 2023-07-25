/*
 * Copyright 2023 Sweden Connect
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.thymeleaf.spring5.SpringTemplateEngine;

import lombok.Setter;
import se.swedenconnect.bankid.idp.authn.BankIdAttributeProducer;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties.RelyingParty;
import se.swedenconnect.bankid.idp.rp.InMemoryRelyingPartyRepository;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.impl.BankIDClientImpl;
import se.swedenconnect.bankid.rpapi.service.impl.ZxingQRGenerator;
import se.swedenconnect.bankid.rpapi.support.WebClientFactoryBean;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.authn.psc.RequestedPrincipalSelection;
import se.swedenconnect.opensaml.sweid.saml2.authn.psc.build.MatchValueBuilder;
import se.swedenconnect.opensaml.sweid.saml2.authn.psc.build.RequestedPrincipalSelectionBuilder;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.factory.PkiCredentialConfigurationProperties;
import se.swedenconnect.security.credential.factory.PkiCredentialFactoryBean;
import se.swedenconnect.spring.saml.idp.config.configurers.Saml2IdpConfigurerAdapter;
import se.swedenconnect.spring.saml.idp.extensions.SignatureMessagePreprocessor;
import se.swedenconnect.spring.saml.idp.response.ThymeleafResponsePage;

/**
 * BankID IdP configuration.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@EnableConfigurationProperties(BankIdConfigurationProperties.class)
public class BankIdConfiguration {

  /**
   * The context path.
   */
  @Setter
  @Value("${server.servlet.context-path:/}")
  private String contextPath;

  /**
   * BankID configuration properties.
   */
  private final BankIdConfigurationProperties properties;

  /**
   * Constructor.
   *
   * @param properties the BankID configuration properties
   */
  public BankIdConfiguration(final BankIdConfigurationProperties properties) {
    this.properties = Objects.requireNonNull(properties, "properties must not be null");
  }

  /**
   * Gets a default {@link SecurityFilterChain} protecting other resources.
   * <p>
   * The chain with order 1 is the Spring Security chain for the SAML IdP ...
   * </p>
   *
   * @param http the HttpSecurity object
   * @return a SecurityFilterChain
   * @throws Exception for config errors
   */
  @Bean
  @Order(2)
  SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) throws Exception {
    http
        .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
        .cors().and()
        .authorizeHttpRequests((authorize) -> authorize
            .antMatchers(this.properties.getAuthn().getAuthnPath() + "/**").permitAll()
            .antMatchers("/images/**", "/error", "/assets/**", "/scripts/**", "/webjars/**", "/view/**", "/api/**",
                "/**/resume")
            .permitAll()
            .anyRequest().denyAll());

    return http.build();
  }

  @Bean
  @Order(1)
  SecurityFilterChain managementFilterChain(final HttpSecurity http) throws Exception {
    http.authorizeHttpRequests()
            .antMatchers("/actuator/**").permitAll();
    return http.build();
  }

  /**
   * Creates the {@link QRGenerator} to use when generating QR code images.
   *
   * @return a {@link QRGenerator}
   */
  @Bean
  QRGenerator qrGenerator() {
    final ZxingQRGenerator generator = new ZxingQRGenerator();
    generator.setDefaultSize(this.properties.getQrCode().getSize());
    generator.setDefaultImageFormat(this.properties.getQrCode().getImageFormat());
    return generator;
  }

  /**
   * Gets the {@link RelyingPartyRepository} bean.
   *
   * @param qrGenerator the {@link QRGenerator} bean
   * @return a {@link RelyingPartyRepository}
   * @throws Exception for errors creating the RP data
   */
  @Bean
  RelyingPartyRepository relyingPartyRepository(final QRGenerator qrGenerator) throws Exception {

    final List<RelyingPartyData> relyingParties = new ArrayList<>();
    for (final RelyingParty rp : this.properties.getRelyingParties()) {

      if (rp.getEntityIds().isEmpty()) {
        if (!this.properties.isTestMode()) {
          throw new IllegalArgumentException(
              "IdP is not in test mode, but Relying Party '%s' does not declare any SP:s".formatted(rp.getId()));
        }
        else if (this.properties.getRelyingParties().size() > 1) {
          throw new IllegalArgumentException("Relying Party '%s' configured to serve all SP:s, but there are more RP"
              + " configurations - This is not permitted");
        }
      }

      final PkiCredential credential = this.createPkiCredential(rp.getCredential());

      final WebClientFactoryBean webClientFactory = new WebClientFactoryBean(
          this.properties.getServiceUrl(), this.properties.getServerRootCertificate(), credential);
      webClientFactory.afterPropertiesSet();

      final BankIDClient client =
          new BankIDClientImpl(rp.getId(), webClientFactory.createInstance(), qrGenerator);

      relyingParties.add(new RelyingPartyData(client, rp.getEntityIds(),
          rp.getUserMessage().getLoginText(), rp.getUserMessage().getFallbackSignText(), rp.getRequirement()));
    }
    return new InMemoryRelyingPartyRepository(relyingParties);
  }

  /**
   * Given a {@link PkiCredentialConfigurationProperties} a {@link PkiCredential} is created.
   *
   * @param cred the properties
   * @return a {@link PkiCredential}
   * @throws Exception for creation errors
   */
  private PkiCredential createPkiCredential(final PkiCredentialConfigurationProperties cred) throws Exception {
    final PkiCredentialFactoryBean factory = new PkiCredentialFactoryBean(cred);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Creates the {@link SimulatedAuthenticationProvider} which is the {@link AuthenticationProvider} that is responsible
   * of the user authentication.
   *
   * @return a {@link SimulatedAuthenticationProvider}
   */
  @Bean
  BankIdAuthenticationProvider bankIdAuthenticationProvider() {
    final BankIdAuthenticationProvider provider = new BankIdAuthenticationProvider(
        this.properties.getAuthn().getAuthnPath(), this.properties.getAuthn().getResumePath(),
        this.properties.getAuthn().getSupportedLoas(), this.properties.getAuthn().getEntityCategories());
    provider.setName(this.properties.getAuthn().getProviderName());
    return provider;
  }

  /**
   * Gets a {@link Saml2IdpConfigurerAdapter} that applies custom configuration for the IdP.
   *
   * @param signMessageProcessor a {@link SignatureMessagePreprocessor} for display of sign messages
   * @return a {@link Saml2IdpConfigurerAdapter}
   */
  @Bean
  Saml2IdpConfigurerAdapter samlIdpSettingsAdapter(final SignatureMessagePreprocessor signMessageProcessor) {
    return (http, configurer) -> {
      configurer
          .authnRequestProcessor(c -> c.authenticationProvider(
              pc -> pc.signatureMessagePreprocessor(signMessageProcessor)))
          .idpMetadataEndpoint(mdCustomizer -> {
            mdCustomizer.entityDescriptorCustomizer(this.metadataCustomizer());
          })
          .userAuthentication(c -> {
            c.attributeProducers(producers -> {
              producers.add(new BankIdAttributeProducer());
            });
          });
    };
  }

  // For customizing the metadata published by the IdP
  //
  private Customizer<EntityDescriptor> metadataCustomizer() {
    return e -> {
      final RequestedPrincipalSelection rps = RequestedPrincipalSelectionBuilder.builder()
          .matchValues(MatchValueBuilder.builder()
              .name(AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER)
              .build())
          .build();

      final IDPSSODescriptor ssoDescriptor = e.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
      Extensions extensions = ssoDescriptor.getExtensions();
      if (extensions == null) {
        extensions = (Extensions) XMLObjectSupport.buildXMLObject(Extensions.DEFAULT_ELEMENT_NAME);
        ssoDescriptor.setExtensions(extensions);
      }
      extensions.getUnknownXMLObjects().add(rps);

      KeyDescriptor encryption = null;
      for (final KeyDescriptor kd : ssoDescriptor.getKeyDescriptors()) {
        if (Objects.equals(UsageType.ENCRYPTION, kd.getUse())) {
          encryption = kd;
          break;
        }
        if (kd.getUse() == null || Objects.equals(UsageType.UNSPECIFIED, kd.getUse())) {
          encryption = kd;
        }
      }
      if (encryption != null) {
        final String[] algs = { "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p",
            "http://www.w3.org/2009/xmlenc11#aes256-gcm",
            "http://www.w3.org/2009/xmlenc11#aes192-gcm",
            "http://www.w3.org/2009/xmlenc11#aes128-gcm"
        };
        for (final String alg : algs) {
          final EncryptionMethod method =
              (EncryptionMethod) XMLObjectSupport.buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
          method.setAlgorithm(alg);
          encryption.getEncryptionMethods().add(method);
        }
      }

    };
  }

  /**
   * A response page using Thymeleaf to post the response.
   *
   * @param templateEngine the template engine
   * @return a {@link ThymeleafResponsePage}
   */
  @Bean
  ThymeleafResponsePage responsePage(final SpringTemplateEngine templateEngine) {
    return new ThymeleafResponsePage(templateEngine, "post-response.html");
  }

}
