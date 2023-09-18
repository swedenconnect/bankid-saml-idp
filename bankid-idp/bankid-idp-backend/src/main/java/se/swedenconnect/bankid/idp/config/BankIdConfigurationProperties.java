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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.audit.AuditEventRepository;
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
import se.swedenconnect.bankid.idp.audit.AbstractBankIdAuditEventRepository;
import se.swedenconnect.bankid.idp.authn.BankIdAuthenticationController;
import se.swedenconnect.bankid.idp.rp.RelyingPartyUiInfo;
import se.swedenconnect.bankid.rpapi.service.QRGenerator;
import se.swedenconnect.bankid.rpapi.service.impl.AbstractQRGenerator;
import se.swedenconnect.bankid.rpapi.support.WebClientFactoryBean;
import se.swedenconnect.opensaml.sweid.saml2.authn.LevelOfAssuranceUris;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.factory.PkiCredentialConfigurationProperties;
import se.swedenconnect.security.credential.factory.PkiCredentialFactoryBean;

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
   * Whether we are using a built-in frontend, i.e., if we are using the built in Vue frontend app, this controller
   * redirects calls made from the underlying SAML IdP library to our frontend start page.
   */
  @Getter
  @Setter
  private boolean builtInFrontend = true;

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
  private final QrCodeConfiguration qrCode = new QrCodeConfiguration();

  /**
   * Configuration for health endpoints.
   */
  @NestedConfigurationProperty
  @Getter
  private final HealthConfiguration health = new HealthConfiguration();

  /**
   * Configuration for audit support.
   */
  @NestedConfigurationProperty
  @Getter
  private final AuditConfiguration audit = new AuditConfiguration();

  /**
   * Configuration concerning the BankID IdP UI (including texts displayed in the BankID app).
   */
  @NestedConfigurationProperty
  @Getter
  private final UiProperties ui = new UiProperties();

  /**
   * The relying parties handled by this IdP.
   */
  @Getter
  private final List<RelyingPartyConfiguration> relyingParties = new ArrayList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (!StringUtils.hasText(this.serviceUrl)) {
      if (!this.testMode) {
        this.serviceUrl = WebClientFactoryBean.TEST_WEB_SERVICE_URL;
      }
      else {
        this.serviceUrl = WebClientFactoryBean.PRODUCTION_WEB_SERVICE_URL;
      }
      log.info("bankid.service-url was not assigned, defaulting to {}", this.serviceUrl);
    }

    if (this.serverRootCertificate == null) {
      if (!this.testMode) {
        this.serverRootCertificate = WebClientFactoryBean.PRODUCTION_ROOT_CERTIFICATE.get();
      }
      else {
        this.serverRootCertificate = WebClientFactoryBean.TEST_ROOT_CERTIFICATE.get();
      }
      log.info("bankid.server-root-certificate was not assigned, defaulting to {}",
          ((ClassPathResource) this.serverRootCertificate).getPath());
    }
    this.authn.afterPropertiesSet();
    this.qrCode.afterPropertiesSet();
    this.health.afterPropertiesSet();
    this.audit.afterPropertiesSet();
    this.ui.afterPropertiesSet();

    Assert.notEmpty(this.relyingParties, "bankid.relying-parties must contain at least one RP");
    for (final RelyingPartyConfiguration rp : this.relyingParties) {
      rp.afterPropertiesSet();

      final RelyingPartyConfiguration.RpUserMessage msg = rp.getUserMessage();

      if (msg.getFallbackSignText() == null) {
        Assert.notNull(this.ui.getUserMessageDefaults().getFallbackSignText(),
            "bankid.user-message-defaults.fallback-sign-text must be assigned");

        msg.setFallbackSignText(this.ui.getUserMessageDefaults().getFallbackSignText());
      }
      if (msg.getLoginText() == null && msg.isInheritDefaultLoginText()
          && this.ui.getUserMessageDefaults().getLoginText() != null) {
        msg.setLoginText(this.ui.getUserMessageDefaults().getLoginText());
      }
    }
  }

  /**
   * QR code configuration.
   */
  @Data
  public static final class QrCodeConfiguration implements InitializingBean {

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
  public static class RelyingPartyConfiguration implements InitializingBean {

    /**
     * The ID for the Relying Party. Used in logging and may be used for statistics.
     */
    @Getter
    @Setter
    private String id;

    /**
     * The SAML entityID:s (SP:s) served by this Relying Party. If the IdP is in test mode this may be an empty list
     * (meaning that all SP:s are served).
     */
    @Getter
    private final List<String> entityIds = new ArrayList<>();

    /**
     * The BankID relying party credential.
     */
    @Getter
    @Setter
    private PkiCredentialConfigurationProperties credential;

    // Internal use only ...
    private PkiCredential _credential;

    /**
     * Relying Party specific display text for authentication (and signature). Overrides the default text.
     */
    @Getter
    @Setter
    private RpUserMessage userMessage;

    /**
     * The UI info for a Relying Party is normally extracted from the SAML metadata, but there are cases where you may
     * want to manually configure these data elements (for example if the metadata does not contain this information, or
     * you simply want to override it). This element holds this information.
     */
    @Getter
    @Setter
    private RelyingPartyUiInfo uiInfo;

    /**
     * Specific BankID requirements for this Relying Party.
     */
    @Getter
    @Setter
    private BankIdRequirement bankidRequirements;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
      Assert.hasText(this.id, "bankid.relying-parties[].id must be set");
      Assert.notNull(this.credential, "bankid.relying-parties[].credential.* must be set");
      if (this.credential.isEmpty()) {
        throw new IllegalArgumentException("bankid.relying-parties[].credential.* must be set");
      }
      if (this.userMessage == null) {
        this.userMessage = new RpUserMessage();
      }
      this.userMessage.afterPropertiesSet();
    }

    /**
     * Creates a {@link PkiCredential} given the {@link PkiCredentialConfigurationProperties}.
     *
     * @return a {@link PkiCredential}
     * @throws Exception for errors creating the object
     */
    public PkiCredential createCredential() throws Exception {
      if (this._credential == null) {
        final PkiCredentialFactoryBean factory = new PkiCredentialFactoryBean(this.credential);
        factory.afterPropertiesSet();
        this._credential = factory.getObject();
      }
      return this._credential;
    }

    /**
     * For configuring user messages per RP.
     */
    public static class RpUserMessage extends UiProperties.UserMessageProperties {

      /**
       * If the default user message login text has been assigned, and a specific RP wishes to not use login messages it
       * should set this flag to {@code false} (and not assign {@code login-text}).
       */
      @Getter
      @Setter
      private boolean inheritDefaultLoginText = true;

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

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws Exception {
      if (!StringUtils.hasText(this.providerName)) {
        this.providerName = "BankID";
        log.info("bankid.authn.provider-name is not assigned, defaulting to '{}'", this.providerName);
      }
      if (!StringUtils.hasText(this.authnPath)) {
        this.authnPath = BankIdAuthenticationController.AUTHN_PATH;
      }
      if (!StringUtils.hasText(this.resumePath)) {
        this.resumePath = "/resume";
      }
      if (this.supportedLoas.isEmpty()) {
        this.supportedLoas.add(LevelOfAssuranceUris.AUTHN_CONTEXT_URI_UNCERTIFIED_LOA3);
        log.info("bankid.authn.supported-loas has not been assigned, defaulting to {}", this.supportedLoas);
      }
    }

  }

  /**
   * Configuration for health endpoints.
   */
  public static class HealthConfiguration implements InitializingBean {

    /**
     * Default value for the setting that tells when the health endpoint should warn about Relying Party certificates
     * that are about to expire.
     */
    public static final Duration RP_CERTIFICATE_WARN_THRESHOLD_DEFAULT = Duration.ofDays(14);

    /**
     * Setting that tells when the health endpoint should warn about Relying Party certificates that are about to
     * expire. The default is 14 days.
     */
    @Getter
    @Setter
    private Duration rpCertificateWarnThreshold;

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws Exception {
      if (this.rpCertificateWarnThreshold == null) {
        this.rpCertificateWarnThreshold = RP_CERTIFICATE_WARN_THRESHOLD_DEFAULT;
      }
    }

  }

  /**
   * Audit logging configuration.
   */
  public static class AuditConfiguration implements InitializingBean {

    /**
     * The type of {@link AuditEventRepository} that should be used. Possible values are: {@code memory} for an
     * in-memory repository, {@code redislist} for a Redis list implementation, {@code redistimeseries} for a Redis time
     * series implementation or {@code other} if you extend the BankID IdP with your own implementation.
     */
    @Getter
    @Setter
    private String repository;

    /**
     * If assigned, the audit events will not only be stored according to the {@code repository} but also be written to
     * the given log file. If set, a complete path must be given.
     */
    @Getter
    @Setter
    private String logFile;

    /**
     * The supported events that will be logged to the given repository (and possibly the file). The default is
     * {@link AbstractBankIdAuditEventRepository#DEFAULT_SUPPORTED_EVENTS}.
     */
    @Getter
    private List<String> supportedEvents = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws Exception {
      if (!StringUtils.hasText(this.repository)) {
        this.repository = "memory";
        log.info("bankid.audit.repository has not been assigned, defaulting to '{}'", this.repository);
      }
      if (this.supportedEvents.isEmpty()) {
        this.supportedEvents = AbstractBankIdAuditEventRepository.DEFAULT_SUPPORTED_EVENTS;
      }
    }

  }

}
