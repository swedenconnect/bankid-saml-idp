/*
 * Copyright 2023 Litsec AB
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
package se.swedenconnect.bankid.rpapi.support;

import java.security.KeyStore;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;
import se.swedenconnect.security.credential.KeyStoreCredential;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.factory.KeyStoreFactoryBean;
import se.swedenconnect.security.credential.utils.X509Utils;

/**
 * Spring factory class for configuring and creating a {@link WebClient} instance that can be used to communicate with
 * the BankID server.
 * 
 * @author Martin Lindström
 */
public class WebClientFactoryBean extends AbstractFactoryBean<WebClient> {

  /** Class logger. */
  private final Logger log = LoggerFactory.getLogger(WebClientFactoryBean.class);

  /** The BankID webservice URL (production). */
  public static final String PRODUCTION_WEB_SERVICE_URL = "https://appapi2.bankid.com/rp/v5.1";

  /** The BankID webservice URL (test). */
  public static final String TEST_WEB_SERVICE_URL = "https://appapi2.test.bankid.com/rp/v5.1";

  /** A resource pointing at the server TLS root certificate for production. */
  public static final Supplier<Resource> PRODUCTION_ROOT_CERTIFICATE =
      () -> new ClassPathResource("trust/bankid-trust-prod.crt");

  /** A resource pointing at the server TLS root certificate for production. */
  public static final Supplier<Resource> TEST_ROOT_CERTIFICATE =
      () -> new ClassPathResource("trust/bankid-trust-test.crt");

  public static final Supplier<Resource> TEST_RP_CREDENTIAL =
      () -> new ClassPathResource("test/FPTestcert4_20220818.p12");

  public static final String TEST_RP_CREDENTIAL_PASSWORD = "qwerty123";

  /** The builder. Defaults to {@link WebClient#builder()}. */
  private WebClient.Builder webClientBuilder;

  /** The base URL for the BankID server. */
  private final String webServiceUrl;

  /** A resource to the root certificate that we trust when verifying the BankID server certificate. */
  private final Resource trustedRoot;

  /** The credential holding the client TLS key and certificate (BankID relying party certificate). */
  private final PkiCredential rpCredential;

  /**
   * Creates a factory bean for creating {@link WebClient}s.
   * 
   * @param webServiceUrl the web service URL to the BankID server - defaults to {@link #PRODUCTION_WEB_SERVICE_URL}
   * @param trustedRoot the resource to the root certificate that we trust when verifying the BankID server certificate
   *          - defaults to {@link #PRODUCTION_ROOT_CERTIFICATE}
   * @param rpCredential the credential holding the client TLS key and certificate (BankID relying party certificate)
   */
  public WebClientFactoryBean(final String webServiceUrl, final Resource trustedRoot,
      final PkiCredential rpCredential) {
    this.webServiceUrl = Optional.ofNullable(webServiceUrl)
        .filter(u -> !StringUtils.hasText(u))
        .orElseGet(() -> {
          log.info("Applying default setting for webServiceUrl: {}", PRODUCTION_WEB_SERVICE_URL);
          return PRODUCTION_WEB_SERVICE_URL;
        });
    this.trustedRoot = Optional.ofNullable(trustedRoot)
        .orElseGet(() -> {
          final Resource r = PRODUCTION_ROOT_CERTIFICATE.get();
          log.info("Applying default setting for the trustedRoot: classpath:{}", ((ClassPathResource) r).getPath());
          return r;
        });
    this.rpCredential = Objects.requireNonNull(rpCredential, "rpCredential must not be null");
  }

  /**
   * Creates a {@link WebClientFactoryBean} with the {@code webServiceUrl} set to {@link #PRODUCTION_WEB_SERVICE_URL}
   * and the {@code trustedRoot} set to {@link #PRODUCTION_ROOT_CERTIFICATE}.
   * 
   * @param rpCredential the credential holding the client TLS key and certificate (BankID relying party certificate)
   * @return a {@link WebClientFactoryBean}
   */
  public static WebClientFactoryBean forProduction(final PkiCredential rpCredential) {
    return new WebClientFactoryBean(null, null, rpCredential);
  }

  /**
   * Creates a {@link WebClientFactoryBean} with the {@code webServiceUrl} set to {@link #TEST_WEB_SERVICE_URL} and the
   * {@code trustedRoot} set to {@link #TEST_ROOT_CERTIFICATE}.
   * 
   * @param rpCredential the credential holding the client TLS key and certificate (BankID relying party certificate)
   * @return a {@link WebClientFactoryBean}
   */
  public static WebClientFactoryBean forTest(final PkiCredential rpCredential) {
    return new WebClientFactoryBean(TEST_WEB_SERVICE_URL, TEST_ROOT_CERTIFICATE.get(), rpCredential);
  }

  /**
   * Creates a {@link WebClientFactoryBean} with the {@code webServiceUrl} set to {@link #TEST_WEB_SERVICE_URL}, the
   * {@code trustedRoot} set to {@link #TEST_ROOT_CERTIFICATE} and the credentials loaded from
   * {@link #TEST_RP_CREDENTIAL}.
   * 
   * @return a {@link WebClientFactoryBean}
   */
  public static WebClientFactoryBean forTest() {
    try {
      final KeyStoreFactoryBean factory =
          new KeyStoreFactoryBean(TEST_RP_CREDENTIAL.get(), TEST_RP_CREDENTIAL_PASSWORD.toCharArray(), "PKCS12");
      factory.afterPropertiesSet();
      final KeyStore ks = factory.getObject();
      String alias = null;
      final Iterator<String> it = ks.aliases().asIterator();
      while (it.hasNext() && alias == null) {
        final String a = it.next();
        if (ks.isKeyEntry(a)) {
          alias = a;
          break;
        }
      }
      if (alias == null) {
        alias = "1";
      }
      final KeyStoreCredential credential =
          new KeyStoreCredential(ks, alias, TEST_RP_CREDENTIAL_PASSWORD.toCharArray());
      credential.init();

      return new WebClientFactoryBean(TEST_WEB_SERVICE_URL, TEST_ROOT_CERTIFICATE.get(), credential);
    }
    catch (final Exception e) {
      throw new SecurityException("Failed to load test credentials", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Class<?> getObjectType() {
    return WebClient.class;
  }

  /** {@inheritDoc} */
  @Override
  protected WebClient createInstance() throws Exception {

    final SslContext sslContext = SslContextBuilder.forClient()
        .keyManager(this.rpCredential.getPrivateKey(), this.rpCredential.getCertificate())
        .trustManager(X509Utils.decodeCertificate(this.trustedRoot))
        .build();

    final HttpClient client = HttpClient.create().secure(spec -> spec.sslContext(sslContext));
    final ClientHttpConnector connector = new ReactorClientHttpConnector(client);

    final WebClient.Builder builder = Optional.ofNullable(this.webClientBuilder)
        .orElseGet(() -> WebClient.builder());

    return builder
        .baseUrl(this.webServiceUrl)
        .clientConnector(connector)
        .build();
  }

  /**
   * Assigns the {@link WebClient.Builder} to use. Defaults to {@link WebClient#builder()}.
   * 
   * @param webClientBuilder a builder
   */
  public void setWebClientBuilder(final WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

}
