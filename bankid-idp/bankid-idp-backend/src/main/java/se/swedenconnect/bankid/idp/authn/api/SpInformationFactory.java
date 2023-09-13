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

import java.util.Optional;
import java.util.function.Predicate;

import se.swedenconnect.spring.saml.idp.authentication.Saml2ServiceProviderUiInfo;

/**
 * Helper class for delivering SP information.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class SpInformationFactory {

  /**
   * Obtains the information for the current SP information.
   *
   * @param uiInfo the SP UI info
   * @return a {@link SpInformation}
   */
  public static SpInformation getSpInformation(final Saml2ServiceProviderUiInfo uiInfo) {
    final SpInformation spInfo = new SpInformation();
    if (uiInfo == null) {
      return spInfo;
    }
    spInfo.setDisplayNames(uiInfo.getDisplayNames());

    // Find the logo with the best height/width ratio ...
    // Try to find something larger than 80px and less than 120px first
    //
    spInfo.setImageUrl(Optional.ofNullable(uiInfo.getLogotype(findBestSize()))
        .map(Saml2ServiceProviderUiInfo.Logotype::getUrl)
        .orElseGet(() -> Optional.ofNullable(uiInfo.getLogotype((l) -> true))
            .map(Saml2ServiceProviderUiInfo.Logotype::getUrl)
            .orElseGet(() -> "")));

    return spInfo;
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
