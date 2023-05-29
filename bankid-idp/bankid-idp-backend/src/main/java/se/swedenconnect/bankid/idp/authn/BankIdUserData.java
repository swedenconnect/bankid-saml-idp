package se.swedenconnect.bankid.idp.authn;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

@AllArgsConstructor
@Data
public class BankIdUserData {
  /*
   * Pair of classes BankIdSessionData & RelyingPartyData
   */
  private final BankIdSessionData bankIdSessionData;
  private final RelyingPartyData relyingPartyData;
}
