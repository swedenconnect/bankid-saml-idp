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
package se.swedenconnect.bankid.idp.authn;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;
import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.sweid.saml2.authn.LevelOfAssuranceUris;
import se.swedenconnect.spring.saml.idp.attributes.UserAttribute;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthentication;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserDetails;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.AbstractUserRedirectAuthenticationProvider;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.ResumedAuthenticationToken;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * The BankID {@link AuthenticationProvider}.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdAuthenticationProvider extends AbstractUserRedirectAuthenticationProvider {

  private static final SimpleDateFormat iso8601DateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

  /**
   * The provider name.
   */
  private String name = "BankID";

  /**
   * The supported LoA:s.
   */
  private final List<String> supportedAuthnContextUris;

  /**
   * Declared/supported entity categories.
   */
  private final List<String> entityCategories;

  static {
    iso8601DateFormatter.setLenient(false);
    iso8601DateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Constructor.
   *
   * @param authnPath                 the path to where we redirect the user for authentication
   * @param resumeAuthnPath           the path that the authentication process uses to redirect the user back after a completed
   *                                  authentication
   * @param supportedAuthnContextUris the supported LoA:s
   * @param entityCategories          declared/supported entity categories
   */
  public BankIdAuthenticationProvider(final String authnPath, final String resumeAuthnPath,
                                      final List<String> supportedAuthnContextUris, final List<String> entityCategories) {
    super(authnPath, resumeAuthnPath);
    this.supportedAuthnContextUris = Optional.ofNullable(supportedAuthnContextUris)
        .filter(s -> !s.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("supportedAuthnContextUris must be set and be non-empty"));
    this.entityCategories = Objects.requireNonNull(entityCategories, "entityCategories must not be null");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Saml2UserAuthentication resumeAuthentication(final ResumedAuthenticationToken token)
      throws Saml2ErrorStatusException {

    final BankIdAuthenticationToken bankIdToken = BankIdAuthenticationToken.class.cast(token.getAuthnToken());
    final CollectResponse authnData = (CollectResponse) bankIdToken.getDetails();
    if (authnData.getCompletionData() == null) {
      throw BankIdAuthenticationExceptionFactory.validationError(authnData.getOrderReference());
    }
    // TODO: Compare pnr from principal selection with pnr from authnData
    // TODO: We should not hardwire loa3-uncertified

    final List<UserAttribute> userAttributes = mapUserAttributes(authnData);
    final Saml2UserDetails userDetails = new Saml2UserDetails(userAttributes,
        AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER,
        LevelOfAssuranceUris.AUTHN_CONTEXT_URI_UNCERTIFIED_LOA3,
        Instant.now(), authnData.getCompletionData().getDevice().getIpAddress());

    Saml2UserAuthentication saml2UserAuthentication = new Saml2UserAuthentication(userDetails);
    if (SecurityContextHolder.getContext().getAuthentication() instanceof Saml2UserAuthenticationInputToken saml && saml.getAuthnRequirements().getSignatureMessageExtension() != null) {
      saml2UserAuthentication.getSaml2UserDetails().setSignMessageDisplayed(true);
      String signature = authnData.getCompletionData().getSignature();
      if (signature == null) {
        throw BankIdAuthenticationExceptionFactory.validationError(authnData.getOrderReference());
      }
      if (Strings.isBlank(signature)) {
        throw BankIdAuthenticationExceptionFactory.invalidSignature(authnData.getOrderReference());
      }
    }
    return saml2UserAuthentication;
  }

  private static List<UserAttribute> mapUserAttributes(final CollectResponse authnData) {
    CompletionData completionData = authnData.getCompletionData();
    final CompletionData.User user = completionData.getUser();

    // Build the authnContextParams attribute ...
    //
    final String authnContextParams = String.format("bankidNotBefore=%s;bankidNotAfter=%s;bankidUserAgentAddress=%s",
        URLEncoder.encode(iso8601DateFormatter.format(new Date(completionData.getCert().getNotBefore())),
            StandardCharsets.UTF_8),
        URLEncoder.encode(iso8601DateFormatter.format(new Date(completionData.getCert().getNotAfter())),
            StandardCharsets.UTF_8),
        URLEncoder.encode(completionData.getDevice().getIpAddress(), StandardCharsets.UTF_8));

    // authnData.getCert().getNotBefore()

    return List.of(
        new UserAttribute(AttributeConstants.ATTRIBUTE_NAME_PERSONAL_IDENTITY_NUMBER,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_PERSONAL_IDENTITY_NUMBER,
            user.getPersonalNumber()),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_DISPLAY_NAME,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_DISPLAY_NAME,
            user.getName()),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_GIVEN_NAME,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_GIVEN_NAME,
            user.getGivenName()),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_SN,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_SN,
            user.getSurname()),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_AUTH_CONTEXT_PARAMS,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_AUTH_CONTEXT_PARAMS,
            authnContextParams),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_USER_SIGNATURE,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_USER_SIGNATURE,
            completionData.getSignature()),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_AUTH_SERVER_SIGNATURE,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_AUTH_SERVER_SIGNATURE,
            completionData.getOcspResponse()),
        new UserAttribute(
            AttributeConstants.ATTRIBUTE_NAME_TRANSACTION_IDENTIFIER,
            AttributeConstants.ATTRIBUTE_FRIENDLY_NAME_TRANSACTION_IDENTIFIER,
            authnData.getOrderReference()
        )
    );
  }

  /**
   * Supports {@link BankIdAuthenticationToken}.
   */
  @Override
  public boolean supportsUserAuthenticationToken(final Authentication authentication) {
    return BankIdAuthenticationToken.class.isInstance(authentication);
  }

  /**
   * Assigns the provider name.
   *
   * @param name the provider name
   */
  public void setName(final String name) {
    if (name != null) {
      this.name = name;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getSupportedAuthnContextUris() {
    return this.supportedAuthnContextUris;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getEntityCategories() {
    return this.entityCategories;
  }

}
