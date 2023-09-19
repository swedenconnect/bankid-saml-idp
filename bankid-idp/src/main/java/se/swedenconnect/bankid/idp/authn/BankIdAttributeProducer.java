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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.saml.saml2.core.Attribute;

import se.swedenconnect.opensaml.sweid.saml2.attribute.AttributeConstants;
import se.swedenconnect.spring.saml.idp.attributes.release.AttributeProducer;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthentication;

/**
 * An {@link AttributeProducer} that makes sure that the {@code authContextParam} and {@code transactionId} attributes
 * are released even if they are not explicitly requested.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class BankIdAttributeProducer implements AttributeProducer {

  /**
   * Will release {@code authContextParam} and {@code transactionId} attributes. If the requester is a sign service, we
   * also release the {@code userSignature} attribute.
   */
  @Override
  public List<Attribute> releaseAttributes(final Saml2UserAuthentication userAuthentication) {

    if (userAuthentication.getSaml2UserDetails().getAttributes().isEmpty()) {
      return Collections.emptyList();
    }
    final List<Attribute> attributes = new ArrayList<>();
    final Attribute authContextParam = userAuthentication.getSaml2UserDetails().getAttributes().stream()
        .filter(a -> AttributeConstants.ATTRIBUTE_NAME_AUTH_CONTEXT_PARAMS.equals(a.getId()))
        .map(a -> a.toOpenSamlAttribute())
        .findFirst()
        .orElse(null);
    if (authContextParam != null) {
      attributes.add(authContextParam);
    }
    final Attribute transactionId = userAuthentication.getSaml2UserDetails().getAttributes().stream()
        .filter(a -> AttributeConstants.ATTRIBUTE_NAME_TRANSACTION_IDENTIFIER.equals(a.getId()))
        .map(a -> a.toOpenSamlAttribute())
        .findFirst()
        .orElse(null);
    if (transactionId != null) {
      attributes.add(transactionId);
    }

    // If this is a signature operation, we always include the userSignature attribute.
    //
    if (userAuthentication.getAuthnRequestToken().isSignatureServicePeer()) {
      final Attribute userSignature = userAuthentication.getSaml2UserDetails().getAttributes().stream()
          .filter(a -> AttributeConstants.ATTRIBUTE_NAME_USER_SIGNATURE.equals(a.getId()))
          .map(a -> a.toOpenSamlAttribute())
          .findFirst()
          .orElse(null);
      if (userSignature != null) {
        attributes.add(userSignature);
      } 
    }

    return attributes;
  }

}
