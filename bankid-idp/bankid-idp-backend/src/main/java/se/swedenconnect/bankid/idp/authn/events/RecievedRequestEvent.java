package se.swedenconnect.bankid.idp.authn.events;

import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

import javax.servlet.http.HttpServletRequest;

public class RecievedRequestEvent extends AbstractBankIdEvent {

  private final PollRequest pollRequest;
  public RecievedRequestEvent(final HttpServletRequest request, final RelyingPartyData data, final PollRequest pollRequest) {
    super(request, data);
    this.pollRequest = pollRequest;
  }

  public PollRequest getPollRequest() {
    return pollRequest;
  }
}
