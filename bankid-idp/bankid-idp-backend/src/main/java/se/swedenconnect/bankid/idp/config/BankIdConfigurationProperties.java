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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationController;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.impl.AbstractQRGenerator;
import se.swedenconnect.bankid.rpapi.support.WebClientFactoryBean;
import se.swedenconnect.security.credential.factory.PkiCredentialConfigurationProperties;

/**
 * BankID configuration properties.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@ConfigurationProperties("bankid")
@Slf4j
public class BankIdConfigurationProperties implements InitializingBean {

  /**
   * The URL to the BankID API. Defaults to {@link WebClientFactoryBean#PRODUCTION_WEB_SERVICE_URL}.
   */
  @Getter
  @Setter
  private String serviceUrl;

  /**
   * The root certificate of the BankID server TLS credential. Defaults to
   * {@code classpath:trust/bankid-trust-prod.crt}.
   */
  @Getter
  @Setter
  private Resource serverRootCertificate;

  /**
   * Should be set to {@code true} if the BankID IdP is running in "test mode", i.e., if the test BankID RP API is used.
   */
  @Getter
  @Setter
  private boolean testMode = false;

  /**
   * IdP Authentication configuration.
   */
  @NestedConfigurationProperty
  @Getter
  private final IdpConfiguration authn = new IdpConfiguration(); 

  /**
   * QR code generation configuration.
   */
  @NestedConfigurationProperty
  @Getter
  private final QrCode qrCode = new QrCode();

  /**
   * The relying parties handled by this IdP.
   */
  @Getter
  private final List<RelyingParty> relyingParties = new ArrayList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (!StringUtils.hasText(this.serviceUrl)) {
      this.serviceUrl = WebClientFactoryBean.PRODUCTION_WEB_SERVICE_URL;
      log.info("bankid.service-url was not assigned, defaulting to {}", this.serviceUrl);
    }

    if (this.serverRootCertificate == null) {
      this.serverRootCertificate = WebClientFactoryBean.PRODUCTION_ROOT_CERTIFICATE.get();
      log.info("bankid.server-root-certificate was not assigned, defaulting to {}",
          ((ClassPathResource) this.serverRootCertificate).getPath());
    }
    this.authn.afterPropertiesSet();
    this.qrCode.afterPropertiesSet();

    Assert.notEmpty(this.relyingParties, "bankid.relying-parties must contain at least one RP");
    for (final RelyingParty rp : this.relyingParties) {
      rp.afterPropertiesSet();
    }
  }

  /**
   * QR code configuration.
   */
  @Data
  public static final class QrCode implements InitializingBean {

    /**
     * The height and width in pixels of the QR code. Defaults to {@link AbstractQRGenerator#DEFAULT_SIZE}.
     */
    private Integer size;

    /**
     * The image format for the generated QR code. Defaults to {@link QRGenerator.ImageFormat#PNG}.
     */
    private QRGenerator.ImageFormat imageFormat;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
      if (this.size == null) {
        this.size = AbstractQRGenerator.DEFAULT_SIZE;
        log.info("bankid.qr-code.size was not assigned, defaulting to {}", this.size);
      }
      if (this.imageFormat == null) {
        this.imageFormat = QRGenerator.ImageFormat.PNG;
        log.info("bankid.qr-code.image-format was not assigned, defaulting to {}", this.imageFormat);
      }
    }
  }

  /**
   * Configuration for a relying party. A BankID Relying Party can serve any number of SAML SP:s (usually they are from
   * the same organization).
   */
  public static class RelyingParty implements InitializingBean {

    /**
     * The ID for the Relying Party. Used in logging and may be used for statistics.
     */
    @Getter
    @Setter
    private String id;

    /**
     * The SAML entityID:s (SP:s) served by this Relying Party. If the IdP is in test mode this may be an empty
     * list (meaning that all SP:s are served).
     */
    @Getter
    private final List<String> entityIds = new ArrayList<>();

    /**
     * The BankID relying party credential.
     */
    @NestedConfigurationProperty
    @Getter
    private PkiCredentialConfigurationProperties credential;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
      Assert.hasText(id, "bankid.relying-parties[].id must be set");
      if (this.credential.isEmpty()) {
        throw new IllegalArgumentException("bankid.relying-parties[].credential.* must be set");
      }
    }
  }

  /**
   * Configuration of the IdP.
   */
  public static class IdpConfiguration implements InitializingBean {

    /**
     * The name of the authentication provider.
     */
    @Getter
    @Setter
    private String providerName;

    /**
     * The authentication path. Where the Spring Security flow directs the user for authentication by our
     * implementation.
     */
    @Getter
    @Setter
    private String authnPath;

    /**
     * The resume path. Where we redirect back the user after that we are done.
     */
    @Getter
    @Setter
    private String resumePath;

    /**
     * The supported LoA:s.
     */
    @Getter
    private final List<String> supportedLoas = new ArrayList<>();

    /**
     * The SAML entity categories this IdP declares.
     */
    @Getter
    private final List<String> entityCategories = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
      if (!StringUtils.hasText(this.providerName)) {
        this.providerName = "BankID";
        log.info("bankid.authn.provider-name is not assigned, defaulting to '{}'", this.providerName);
      }
      if (!StringUtils.hasText(this.authnPath)) {
        this.authnPath = BankIdAuthenticationController.AUTHN_PATH;
      }
      Assert.hasText(this.resumePath, "bankid.authn.resume-path must be set");
      Assert.notEmpty(this.supportedLoas, "At least one URI must be assigned to bankid.authn.supported-loas");
    }

  }

}
