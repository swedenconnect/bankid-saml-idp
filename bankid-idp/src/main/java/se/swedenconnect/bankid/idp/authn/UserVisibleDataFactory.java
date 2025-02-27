/*
 * Copyright 2023-2025 Sweden Connect
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessageMimeTypeEnum;

/**
 * A utility class that builds the correct messages to display for the user during signing and authentication.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
public class UserVisibleDataFactory {

  /**
   * Constructs a message to be displayed in the BankID app. For signing, we also set the non-visible data.
   *
   * @param context the BankID context
   * @param relyingParty the RP
   * @return a {@code UserVisibleData}
   */
  public static UserVisibleData constructMessage(final BankIdContext context, final RelyingPartyData relyingParty) {

    if (context.getOperation() == BankIdOperation.SIGN) {
      final DataToSign message = new DataToSign();

      // Set the sign display message ...
      //
      if (context.getSignMessage() != null) {
        message.setUserVisibleData(context.getSignMessage().getMessage());

        if (SignMessageMimeTypeEnum.TEXT_MARKDOWN == context.getSignMessage().getMimeType()) {
          message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
        }
      }
      else {
        message.setDisplayText(relyingParty.getFallbackSignText().getText());
        if (DisplayText.TextFormat.SIMPLE_MARKDOWN_V1 == relyingParty.getFallbackSignText().getFormat()) {
          message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
        }
      }
      // Build userNonVisibleData according to 4.2.1.2 of "Implementation Profile for BankID Identity Providers
      // within the Swedish eID Framework".
      //
      final String nonVisibleData = "entityID=%s;authnRequestID=%s".formatted(
          URLEncoder.encode(context.getClientId(), StandardCharsets.UTF_8),
          URLEncoder.encode("authId", StandardCharsets.UTF_8));

      message.setUserNonVisibleData(Base64.getEncoder().encodeToString(nonVisibleData.getBytes()));

      return message;
    }
    else {
      if (relyingParty.getLoginText() == null) {
        return null;
      }
      final UserVisibleData message = new UserVisibleData();
      message.setDisplayText(relyingParty.getLoginText().getText());
      if (DisplayText.TextFormat.SIMPLE_MARKDOWN_V1 == relyingParty.getLoginText().getFormat()) {
        message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
      }
      return message;
    }
  }
}
