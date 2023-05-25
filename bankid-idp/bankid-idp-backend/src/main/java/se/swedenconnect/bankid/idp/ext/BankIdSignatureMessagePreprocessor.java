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
package se.swedenconnect.bankid.idp.ext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatus;
import se.swedenconnect.spring.saml.idp.error.Saml2ErrorStatusException;
import se.swedenconnect.spring.saml.idp.extensions.SignatureMessagePreprocessor;

/**
 * A {@link SignatureMessagePreprocessor} for BankID signature messages.
 * 
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
public class BankIdSignatureMessagePreprocessor implements SignatureMessagePreprocessor {

  /**
   * Procects from displaying sign messages in HTML.
   */
  @Override
  public String processSignMessage(final String encodedMessage, final SignMessageMimeTypeEnum messageType)
      throws Saml2ErrorStatusException {

    if (messageType != null && messageType == SignMessageMimeTypeEnum.TEXT_HTML) {
      throw new Saml2ErrorStatusException(Saml2ErrorStatus.SIGN_MESSAGE,
          "BankID IdP does not support display of HTML SignMessage");
    }

    return new String(Base64.getDecoder().decode(encodedMessage), StandardCharsets.UTF_8);
  }

}
