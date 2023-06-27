package se.swedenconnect.bankid.idp.authn.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionState;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;

import javax.servlet.http.HttpServletRequest;

@Builder
@AllArgsConstructor
@Data
public class PollRequest {
  private final HttpServletRequest request;
  private final Boolean qr;
  private final BankIdSessionState state;
  private final RelyingPartyData relyingPartyData;
  private final UserVisibleData data;
  private final BankIdContext context;
}
