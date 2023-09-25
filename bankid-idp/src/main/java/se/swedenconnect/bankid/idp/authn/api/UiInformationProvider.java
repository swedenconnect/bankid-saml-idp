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
package se.swedenconnect.bankid.idp.authn.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import se.swedenconnect.bankid.idp.config.UiProperties;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyUiInfo;
import se.swedenconnect.spring.saml.idp.authentication.Saml2ServiceProviderUiInfo;

/**
 * Bean providing UI information for consumption by the frontend.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class UiInformationProvider {

  /** The UI properties. */
  private final UiProperties uiProperties;

  /**
   * Constructor.
   *
   * @param uiProperties the UI properties
   */
  public UiInformationProvider(final UiProperties uiProperties) {
    this.uiProperties = uiProperties;
  }

  /**
   * Gets the UI information to display.
   *
   * @param uiInfo the SP UI info
   * @param relyingParty the RP data
   * @return an {@link UiInformation}
   */
  public UiInformation getUiInformation(final Saml2ServiceProviderUiInfo uiInfo, final RelyingPartyData relyingParty) {
    return UiInformation.builder()
        .sp(this.getSpInformation(uiInfo, relyingParty))
        .displayQrHelp(this.uiProperties.isDisplayQrHelp())
        .accessibilityReportLink(this.uiProperties.getAccessibilityReportLink())
        .build();
  }

  /**
   * Gets the provider logotype to display in the UI footer.
   *
   * @return a logotype as byte array
   * @throws IOException see {@link IOUtils} method toByteArray(InputStream inputStream)
   */
  public byte[] getProviderLogo() throws IOException {
    if (Objects.nonNull(this.uiProperties.getProviderSvgLogotype())) {
      return IOUtils.toByteArray(this.uiProperties.getProviderSvgLogotype().getInputStream());
    }
    // Otherwise. Deliver an invisible SVG ...
    final ClassPathResource classPathResource = new ClassPathResource("static/images/transparent.svg");
    return IOUtils.toByteArray(classPathResource.getInputStream());
  }

  /**
   * Obtains the information for the current SP information.
   *
   * @param uiInfo the SP UI info
   * @param relyingParty the RP data
   * @return a {@link SpInformation}
   */
  public SpInformation getSpInformation(
      final Saml2ServiceProviderUiInfo uiInfo, final RelyingPartyData relyingParty) {

    final RelyingPartyUiInfo rpUiInfo = relyingParty.getUiInfo();

    if (uiInfo == null && rpUiInfo == null) {
      return new SpInformation();
    }

    final Map<String, String> displayNames = new HashMap<>();
    String logoUrl;

    if (rpUiInfo != null && !rpUiInfo.isUseAsFallback()) {
      displayNames.putAll(rpUiInfo.getDisplayName());
      logoUrl = rpUiInfo.getLogotypeUrl();

      // Check if there any display names from SAML metadata that aren't set in RP UI ...
      //
      uiInfo.getDisplayNames().entrySet().stream()
          .filter(e -> !displayNames.containsKey(e.getKey()))
          .forEach(e -> displayNames.put(e.getKey(), e.getValue()));

      if (!StringUtils.hasText(logoUrl)) {
        logoUrl = getImageUrl(uiInfo);
      }

      return new SpInformation(displayNames, logoUrl, this.uiProperties.isShowSpMessage());
    }

    displayNames.putAll(uiInfo.getDisplayNames());
    logoUrl = getImageUrl(uiInfo);

    if (rpUiInfo != null) { // fallback

      Optional.ofNullable(rpUiInfo.getDisplayName())
          .ifPresent(dn -> dn.entrySet().stream()
              .filter(e -> !displayNames.containsKey(e.getKey()))
              .forEach(e -> displayNames.put(e.getKey(), e.getValue())));

      if (logoUrl == null) {
        logoUrl = Optional.ofNullable(rpUiInfo.getLogotypeUrl())
            .orElseGet(() -> "");
      }
    }

    return new SpInformation(displayNames, logoUrl, this.uiProperties.isShowSpMessage());
  }

  private static String getImageUrl(final Saml2ServiceProviderUiInfo uiInfo) {

    // Find the logo with the best height/width ratio ...
    // Try to find something larger than 80px and less than 120px first
    //
    return Optional.ofNullable(uiInfo.getLogotype(findBestSize()))
        .map(Saml2ServiceProviderUiInfo.Logotype::getUrl)
        .orElseGet(() -> Optional.ofNullable(uiInfo.getLogotype((l) -> true))
            .map(Saml2ServiceProviderUiInfo.Logotype::getUrl)
            .orElse(null));
  }

  private static Predicate<Saml2ServiceProviderUiInfo.Logotype> findBestSize() {
    return (logo) -> {
      if (logo.getHeight() == null) {
        return false;
      }
      if (logo.getHeight() > 80 && logo.getHeight() < 120) {
        return true;
      }
      return false;
    };
  }

}
