package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.idp.authn.context.BankIdContext;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.service.UserVisibleData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;

public interface BankIdSessionReader {
    /**
     * Loads session data
     *
     * @param request The current request for the user to determine session key
     * @return BankIdSessionState
     */
    BankIdSessionState loadSessionData(HttpServletRequest request);

    /**
     * Loads final CollectResponse
     *
     * @param request The current request for the user to determine session key
     * @return Final CollectResponse from BankidApi containing CompletionData
     */
    CollectResponse laodCompletionData(HttpServletRequest request);

    /**
     * Loads device selection
     *
     * @param request The current request for the user to determine session key
     * @return Device selection from last successful authentication for the current user
     */
    PreviousDeviceSelection loadPreviousSelectedDevice(HttpServletRequest request);

    /**
     * Loads uservisible data
     * @param request The current request for the user to determine session key
     * @return User visible data to be displayed in app
     */
  UserVisibleData loadUserVisibleData(HttpServletRequest request);

  BankIdContext loadContext(HttpServletRequest request);
}
