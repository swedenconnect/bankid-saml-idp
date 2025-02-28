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
package se.swedenconnect.bankid.idp.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;
import se.swedenconnect.bankid.idp.authn.BankIdAttributeProducer;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationProvider;
import se.swedenconnect.bankid.idp.authn.api.UiInformationProvider;
import se.swedenconnect.bankid.idp.authn.error.ErrorhandlerFilter;
import se.swedenconnect.bankid.idp.authn.events.BankIdEventPublisher;
import se.swedenconnect.bankid.idp.authn.service.BankIdRequestFactory;
import se.swedenconnect.bankid.idp.authn.service.BankIdService;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties.RelyingPartyConfiguration;
import se.swedenconnect.bankid.idp.rp.DefaultRelyingPartyRepository;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyRepository;
import se.swedenconnect.bankid.rpapi.service.BankIDClient;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.impl.BankIDClientImpl;
import se.swedenconnect.bankid.rpapi.service.impl.ZxingQRGenerator;
import se.swedenconnect.bankid.rpapi.support.WebClientFactoryBean;
import se.swedenconnect.security.credential.factory.PkiCredentialFactory;
import se.swedenconnect.spring.saml.idp.config.configurers.Saml2IdpConfigurerAdapter;
import se.swedenconnect.spring.saml.idp.extensions.SignatureMessagePreprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * BankID IdP configuration.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@EnableConfigurationProperties(BankIdConfigurationProperties.class)
public class BankIdConfiguration {

  /** BankID configuration properties. */
  private final BankIdConfigurationProperties properties;

  /** For loading credentials. */
  private final PkiCredentialFactory pkiCredentialFactory;

  /**
   * Constructor.
   *
   * @param properties the BankID configuration properties
   * @param pkiCredentialFactory for loading credentials
   */
  public BankIdConfiguration(final BankIdConfigurationProperties properties,
      final PkiCredentialFactory pkiCredentialFactory) {
    this.properties = Objects.requireNonNull(properties, "properties must not be null");
    this.pkiCredentialFactory = Objects.requireNonNull(pkiCredentialFactory, "pkiCredentialFactory must not be null");
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
        .securityContext(sc -> sc.requireExplicitSave(false))
        .csrf(csrf -> {
          csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
          final CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
          requestHandler.setCsrfRequestAttributeName(null);
          csrf.csrfTokenRequestHandler(requestHandler);
        })
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers(this.internalEndpoints()).permitAll()
            .requestMatchers(this.properties.getAuthn().getAuthnPath()).permitAll()
            .requestMatchers(this.frontendRoutes(this.properties.getAuthn().getAuthnPath())).permitAll()
            .anyRequest().denyAll());

    return http.build();
  }

  private String[] internalEndpoints() {
    return new String[] { "/api/**", "/logo.svg", "/favicon.png", "/favicon.svg", "/assets/**", "/view/**",
        "/images/**" };
  }

  /**
   * @return array of routes that needs to be forwarded to frontend
   * @param authnPath
   */
  public static String[] frontendRoutes(final String authnPath) {
    return Stream.of("/", "/auto", "/qr", "/error/**", "/error").map(route -> {
      if (authnPath.equals("/")) {
        return route;
      }
      return authnPath + route;
    }).toList().toArray(new String[] {});
  }

  /**
   * Gets the {@link UiProperties.UserErrorProperties} bean.
   *
   * @return {@link UiProperties.UserErrorProperties} bean
   */
  @Bean
  UiProperties.UserErrorProperties userErrorProperties() {
    return this.properties.getUi().getUserError();
  }

  /**
   * Creates the {@link QRGenerator} to use when generating QR code images.
   *
   * @return a {@link QRGenerator}
   */
  @Bean
  QRGenerator qrGenerator() {
    final ZxingQRGenerator generator = new ZxingQRGenerator();
    generator.setDefaultSize(this.properties.getUi().getQrCode().getSize());
    generator.setDefaultImageFormat(this.properties.getUi().getQrCode().getImageFormat());
    return generator;
  }

  /**
   * Gets the bankIdWebClientFactory bean
   *
   * @return lambda function to create webclient from RelyingParty
   */

  @Bean
  Function<RelyingPartyConfiguration, WebClient> bankIdWebClientFactory() {
    return rp -> {
      try {
        final WebClientFactoryBean webClientFactory =
            new WebClientFactoryBean(this.properties.getServiceUrl(), this.properties.getServerRootCertificate(),
                this.pkiCredentialFactory.createCredential(rp.getCredential()));
        webClientFactory.afterPropertiesSet();
        return webClientFactory.createInstance();
      }
      catch (final Exception e) {
        throw new RuntimeException("Failed to create bean for webclient supplier ", e); // TODO: 2023-09-11 Better
      }
    };
  }

  /**
   * Gets the {@link RelyingPartyRepository} bean.
   *
   * @param qrGenerator the {@link QRGenerator} bean
   * @param webClientFactory the WebClientMapper bean (function to create webclient from RelyingParty)
   * @return a {@link RelyingPartyRepository}
   */
  @Bean
  RelyingPartyRepository relyingPartyRepository(final QRGenerator qrGenerator,
      final Function<RelyingPartyConfiguration, WebClient> webClientFactory) {

    final List<RelyingPartyData> relyingParties = new ArrayList<>();
    for (final RelyingPartyConfiguration rp : this.properties.getRelyingParties()) {

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

      final BankIDClient client = new BankIDClientImpl(rp.getId(), webClientFactory.apply(rp), qrGenerator);

      relyingParties.add(new RelyingPartyData(client, rp.getEntityIds(),
          rp.getUserMessage().getLoginText(), rp.getUserMessage().getFallbackSignText(),
          rp.getUiInfo(), rp.getBankidRequirements()));
    }
    return new DefaultRelyingPartyRepository(relyingParties);
  }

  /**
   * Creates the {@link BankIdAuthenticationProvider} which is the {@link AuthenticationProvider} that is responsible
   * for the user authentication.
   *
   * @return a {@link BankIdAuthenticationProvider}
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
    return (http, configurer) -> configurer
        .authnRequestProcessor(c -> c.authenticationProvider(
            pc -> pc.signatureMessagePreprocessor(signMessageProcessor)))
        .userAuthentication(c -> c.attributeProducers(producers -> producers.add(new BankIdAttributeProducer())));
  }

  /**
   * Creates the bean for providing UI information to the frontend.
   *
   * @return a {@link UiInformationProvider}
   */
  @Bean
  UiInformationProvider uiInformationProvider() {
    return new UiInformationProvider(this.properties.getUi(), this.properties.getStartRetryDuration().toMinutes());
  }

  @Bean
  FilterRegistrationBean<OncePerRequestFilter> errorHandlerFilterRegistration(final ErrorhandlerFilter filter) {
    final FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setUrlPatterns(List.of("/view/**"));
    registration.setOrder(Integer.MIN_VALUE + 1);
    registration.setName("ERROR_HANDLER_FILTER_REGISTRATION");
    return registration;
  }

  @Bean
  FilterRegistrationBean<OncePerRequestFilter> csrfFilterRegistration() {
    final FilterRegistrationBean<OncePerRequestFilter> registration =
        new FilterRegistrationBean<>(new CsrfCookieFilter());
    registration.setOrder(SecurityWebFiltersOrder.REACTOR_CONTEXT.getOrder());
    registration.setName("CSRF_HANDLER_FILTER_REGISTRATION");
    return registration;
  }

  @Bean
  BankIdService bankIdService(final BankIdEventPublisher publisher, final CircuitBreaker circuitBreaker,
      final BankIdRequestFactory factory, final BankIdConfigurationProperties properties) {
    return new BankIdService(publisher, circuitBreaker, factory, properties.getStartRetryDuration());
  }

}
