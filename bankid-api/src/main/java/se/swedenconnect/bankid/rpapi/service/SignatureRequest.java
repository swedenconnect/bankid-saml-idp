package se.swedenconnect.bankid.rpapi.service;

import se.swedenconnect.bankid.rpapi.types.BankIDException;
import se.swedenconnect.bankid.rpapi.types.Requirement;

public class SignatureRequest {
  private final String personalIdentityNumber;
  private final String endUserIp;
  private final DataToSign dataToSign;
  private final Requirement requirement;

  /**
   * Request a signing order.
   *
   * @param personalIdentityNumber the ID number of the user trying to be authenticated (optional). If the ID number is
   *          omitted the user must use the same device and the client must be started with the autoStartToken returned
   *          in orderResponse
   * @param endUserIp the user IP address as seen by the relying party
   * @param dataToSign the data to sign
   * @param requirement used by the relying party to set requirements how the authentication or sign operation must be
   *          performed. Default rules are applied if omitted
   * @throws BankIDException for errors
   */
  public SignatureRequest(final String personalIdentityNumber, final String endUserIp, final DataToSign dataToSign, final Requirement requirement) {
    this.personalIdentityNumber = personalIdentityNumber;
    this.endUserIp = endUserIp;
    this.dataToSign = dataToSign;
    this.requirement = requirement;
  }

  public String getPersonalIdentityNumber() {
    return personalIdentityNumber;
  }

  public String getEndUserIp() {
    return endUserIp;
  }

  public DataToSign getDataToSign() {
    return dataToSign;
  }

  public Requirement getRequirement() {
    return requirement;
  }
}
