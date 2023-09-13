package se.swedenconnect.bankid.idp.integration.client;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import se.swedenconnect.bankid.idp.integration.TestSp;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext;
import se.swedenconnect.opensaml.sweid.saml2.request.SwedishEidAuthnRequestGeneratorContext;
import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.Message;
import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessage;
import se.swedenconnect.opensaml.sweid.saml2.signservice.dss.SignMessageMimeTypeEnum;
import se.swedenconnect.opensaml.sweid.saml2.signservice.sap.SADRequest;

public class SAMLContexts {

  public static AuthnRequestGeneratorContext getContext(boolean sign, String idpEntityId) {
    if (sign) {
      return signContext(idpEntityId);
    }
    return authContext();
  }

  public static AuthnRequestGeneratorContext authContext() {
    return new AuthnRequestGeneratorContext() {

      @Override
      public String getPreferredBinding() {
        return SAMLConstants.SAML2_POST_BINDING_URI;
      }
    };
  }

  public static AuthnRequestGeneratorContext signContext(String idpEntityId) {
    return new SwedishEidAuthnRequestGeneratorContext() {

      @Override
      public String getPreferredBinding() {
        return SAMLConstants.SAML2_POST_BINDING_URI;
      }

      @Override
      public SignMessageBuilderFunction getSignMessageBuilderFunction() {
        return (metadata, signMessageEncrypter) -> {
          final SignMessage signMessage =
              (SignMessage) XMLObjectSupport.buildXMLObject(SignMessage.DEFAULT_ELEMENT_NAME);
          signMessage.setDisplayEntity(idpEntityId);
          signMessage.setMimeType(SignMessageMimeTypeEnum.TEXT);
          signMessage.setMustShow(true);
          final Message msg = (Message) XMLObjectSupport.buildXMLObject(Message.DEFAULT_ELEMENT_NAME);
          msg.setContent("This is a sign message");
          signMessage.setMessage(msg);
          if (signMessageEncrypter != null) {
            try {
              signMessageEncrypter.encrypt(signMessage, idpEntityId);
            } catch (EncryptionException e) {
            }
          }
          return signMessage;
        };
      }

      @Override
      public AuthnRequestCustomizer getAuthnRequestCustomizer() {
        return (authnRequest) -> {
          final SADRequest sadRequest = (SADRequest) XMLObjectSupport.buildXMLObject(SADRequest.DEFAULT_ELEMENT_NAME);
          sadRequest.setID("ABCDEF");
          sadRequest.setDocCount(4);
          sadRequest.setRequesterID(TestSp.SIGN_ID);
          sadRequest.setSignRequestID("123456789");
          if (authnRequest.getExtensions() == null) {
            final Extensions extensions = (Extensions) XMLObjectSupport.buildXMLObject(Extensions.DEFAULT_ELEMENT_NAME);
            authnRequest.setExtensions(extensions);
          }
          authnRequest.getExtensions().getUnknownXMLObjects().add(sadRequest);
        };
      }

    };
  }
}
