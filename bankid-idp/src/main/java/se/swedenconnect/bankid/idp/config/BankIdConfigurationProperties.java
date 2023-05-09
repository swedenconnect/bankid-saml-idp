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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.Data;
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
@Data
@Slf4j
public class BankIdConfigurationProperties implements InitializingBean {
  
  /**
   * The URL to the BankID API. Defaults to {@link WebClientFactoryBean#PRODUCTION_WEB_SERVICE_URL}.
   */
  private String serviceUrl;

  /**
   * The root certificate of the BankID server TLS credential. Defaults to
   * {@code classpath:trust/bankid-trust-prod.crt}.
   */
  private Resource serverRootCertificate;
  
  /**
   * IdP Authentication configuration.
   */
  private IdpConfiguration authn;
    
  /**
   * QR code generation configuration.
   */
  private QrCode qrCode;
  
  /**
   * Instead of configuring a credential for each relying party shared credentials may be set
   * up. This is maninly useful during testing when a common RP certificate is being used, but
   * also in cases where several SAML entities should use the same RP certificate, for example
   * if one organization has several SAML SP:s but only one BankID relying party credential.
   * This field holds a map of credential names and credentials.
   */
  private Map<String, PkiCredentialConfigurationProperties> sharedCredentials;
  
  /**
   * The relying parties handled by this IdP.
   */
  private List<RelyingParty> relyingParties;
  

  /** {@inheritDoc} */
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
    
    Assert.notNull(this.authn, "bankid.authn.* must be set");
    this.authn.afterPropertiesSet();
    
    if (this.qrCode == null) {
      this.qrCode = new QrCode();
    }
    this.qrCode.afterPropertiesSet();
    
    if (this.sharedCredentials == null) {
      this.sharedCredentials = Collections.emptyMap();
    }
    
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

    /** {@inheritDoc} */
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
   * Configuration for a relying party.
   */
  @Data
  public static class RelyingParty implements InitializingBean {

    /**
     * The SAML entityID of the client.
     */
    private String entityId;
    
    /**
     * The BankID relying party credential.
     */
    private PkiCredentialConfigurationProperties credential;
    
    /**
     * Instead of configuring a credential for each relying party a credential reference may
     * be given. This fields must point to an entry configured under {@code bankid.shared-credentials}.
     */
    private String credentialRef;

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws Exception {
      Assert.hasText(this.entityId, "bankid.relying-parties[].entity-id must be set");
      if (this.credential == null && !StringUtils.hasText(this.credentialRef)) {
        throw new IllegalArgumentException("No credential given for relying party");
      }
      else if (this.credential != null && StringUtils.hasText(this.credentialRef)) {
        throw new IllegalArgumentException("Both credential and credential-ref given - only one can be supplied");
      }
    }

  }
  
  /**
   * Configuration of the IdP.
   */
  @Data
  public static class IdpConfiguration implements InitializingBean {
    
    /**
     * The name of the authentication provider.
     */
    private String providerName;
    
    /** 
     * The authentication path. Where the Spring Security flow directs the user for authentication by our implementation. 
     */
    private String authnPath;
    
    /** 
     * The resume path. Where we redirect back the user after that we are done.  
     */
    private String resumePath;
    
    /**
     * The supported LoA:s.
     */
    private List<String> supportedLoas;
    
    /**
     * The SAML entity categories this IdP declares.
     */
    private List<String> entityCategories;

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
      Assert.hasText(this.resumePath, "bankid.authn.resume-path must be set");
      Assert.notEmpty(this.supportedLoas, "At least one URI must be assigned to bankid.authn.supported-loas");
      if (this.entityCategories == null) {
        this.entityCategories = Collections.emptyList();
      }
    }

  }
  
}
