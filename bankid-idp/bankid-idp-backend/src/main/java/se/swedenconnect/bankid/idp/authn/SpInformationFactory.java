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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.core.xml.LangBearing;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSURIImpl;
import org.opensaml.saml.ext.saml2mdui.impl.LogoImpl;

import se.swedenconnect.spring.saml.idp.response.Saml2ResponseAttributes;

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
   * @param request the servlet request
   * @return a {@link SpInformation}
   */
  public static SpInformation getSpInformation(final HttpServletRequest request) {
    final Saml2ResponseAttributes attribute = (Saml2ResponseAttributes) request.getSession()
        .getAttribute("se.swedenconnect.spring.saml.idp.web.filters.ResponseAttributes");
    if (attribute == null) {
      return new SpInformation();
    }
    final Map<String, String> displayNames = attribute.getPeerMetadata().getOrganization().getDisplayNames()
        .stream()
        .filter(v -> v.getXMLLang() != null && v.getValue() != null)
        .collect(Collectors.toMap(LangBearing::getXMLLang, XSString::getValue));
    final List<LogoImpl> images = Optional.ofNullable(attribute.getPeerMetadata().getRoleDescriptors().get(0))
        .flatMap(d -> Optional.ofNullable(d.getExtensions().getUnknownXMLObjects().get(0)))
        .flatMap(e -> Optional.ofNullable(e.getOrderedChildren()))
        .map(c -> c.stream()
            .filter(x -> x instanceof LogoImpl)
            .map(LogoImpl.class::cast)
            .toList())
        .orElseGet(List::of);
    return new SpInformation(displayNames, Optional.ofNullable(images.get(0)).map(XSURIImpl::getURI).orElse(""));
  }
}
