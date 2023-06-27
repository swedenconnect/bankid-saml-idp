package se.swedenconnect.bankid.idp.authn.service;

import se.swedenconnect.bankid.rpapi.service.AuthenticateRequest;
import se.swedenconnect.bankid.rpapi.service.DataToSign;
import se.swedenconnect.bankid.rpapi.service.SignatureRequest;
import se.swedenconnect.bankid.rpapi.types.Requirement;

public class BankIdRequestFactory {

  public static AuthenticateRequest createAuthenticateRequest(PollRequest request) {
    return new AuthenticateRequest(request.getContext().getPersonalNumber(), request.getRequest().getRemoteAddr(), request.getData(), new Requirement());
  }
  // TODO: 2023-06-26 Requriement Factory

  public static SignatureRequest createSignRequest(PollRequest request) {
    if (request.getData() instanceof DataToSign dataToSign) {
      return new SignatureRequest(request.getContext().getPersonalNumber(), request.getRequest().getRemoteAddr(), dataToSign, new Requirement());
    } else {
      throw new IllegalArgumentException("Message was not of type DataToSign but was " + request.getData().getClass().getCanonicalName());
    }
  }
}
