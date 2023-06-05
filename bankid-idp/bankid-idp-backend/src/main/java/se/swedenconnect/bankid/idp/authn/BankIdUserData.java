package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

@AllArgsConstructor
@Data
public class BankIdUserData {
  /*
   * Meta container for session data
   */
  private BankIdSessionData bankIdSessionData;
  private RelyingPartyData relyingPartyData;

}
