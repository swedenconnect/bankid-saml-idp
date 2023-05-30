package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpServletRequest;

public interface BankIdSessionReader {
    BankIdSessionState loadSessionData(HttpServletRequest request);

    CompletionData laodCompletionData(HttpServletRequest request);
}
