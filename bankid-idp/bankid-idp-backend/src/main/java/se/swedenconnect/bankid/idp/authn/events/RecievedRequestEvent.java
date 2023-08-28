package se.swedenconnect.bankid.idp.authn.events;

import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

import javax.servlet.http.HttpServletRequest;

public class RecievedRequestEvent extends AbstractBankIdEvent {
  public RecievedRequestEvent(HttpServletRequest request, RelyingPartyData data) {
    super(request, data);
  }
}
