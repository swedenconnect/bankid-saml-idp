package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;

public interface BankIdSessionWriter {

  /**
   * Saves BankidSessionData to repository
   * Overwrites data if order reference is the same
   *
   * @param request to determine session key
   * @param data to be saved
   */
  void save(HttpServletRequest request, BankIdSessionData data);

  /**
   * Saves the final CollectResponse that contains CompletionData
   *
   * @param request to determine session key
   * @param data to be saved
   */
  void save(HttpServletRequest request, CollectResponse data);

  /**
   * Deletes everything except device selection for finalized authentication
   *
   * @param request to determine session key
   */
  void delete(HttpServletRequest request);

  /**
   * Loads previous device selection from successful authentication
   *
   * @param request to determine session key
   * @param previousDeviceSelection Device used for authentication
   */
  void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection);

  /**
   * Loads user visible data to be displayed in BankId application
   *
   * @param request to determine session key
   * @param userVisibleData message that should be displayed in app
   */
  void save(HttpServletRequest request, UserVisibleData userVisibleData);
}
