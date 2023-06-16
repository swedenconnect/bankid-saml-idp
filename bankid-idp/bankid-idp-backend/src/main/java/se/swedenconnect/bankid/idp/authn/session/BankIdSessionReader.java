package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpServletRequest;

public interface BankIdSessionReader {
    BankIdSessionState loadSessionData(HttpServletRequest request);

    CollectResponse laodCompletionData(HttpServletRequest request);

    PreviousDeviceSelection loadPreviousSelectedDevice(HttpServletRequest request);
}
