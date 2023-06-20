package se.swedenconnect.bankid.idp.authn;

import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.BankIdOperation;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.spring.saml.idp.authentication.Saml2UserAuthenticationInputToken;

import java.util.Base64;

public class UserVisibleDataFactory {
  public static UserVisibleData constructMessage(final BankIdContext context, final Saml2UserAuthenticationInputToken token,
                                                  final RelyingPartyData relyingParty) {
    if (context.getOperation() == BankIdOperation.SIGN) {
      final DataToSign message = new DataToSign();
      if (token.getAuthnRequirements().getSignatureMessageExtension() != null) {
        message.setUserVisibleData(token.getAuthnRequirements().getSignatureMessageExtension().getMessage());
        if (SignMessageMimeTypeEnum.TEXT_MARKDOWN
            .equals(token.getAuthnRequirements().getSignatureMessageExtension().getMimeType())) {
          message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
        }
      } else {
        message.setDisplayText(relyingParty.getFallbackSignText().getText());
        if (DisplayText.TextFormat.SIMPLE_MARKDOWN_V1.equals(relyingParty.getFallbackSignText().getFormat())) {
          message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
        }
      }
      // TODO: Build userNonVisibleData according to 4.2.1.2 of "Implementation Profile for BankID Identity Providers
      // within the Swedish eID Framework".
      //
      message.setUserNonVisibleData(Base64.getEncoder().encodeToString("TODO".getBytes()));

      return message;
    } else {
      if (relyingParty.getLoginText() == null) {
        return null;
      }
      final UserVisibleData message = new UserVisibleData();
      message.setDisplayText(relyingParty.getLoginText().getText());
      if (DisplayText.TextFormat.SIMPLE_MARKDOWN_V1.equals(relyingParty.getLoginText().getFormat())) {
        message.setUserVisibleDataFormat(UserVisibleData.VISIBLE_DATA_FORMAT_SIMPLE_MARKDOWN_V1);
      }
      return message;
    }
  }
}
