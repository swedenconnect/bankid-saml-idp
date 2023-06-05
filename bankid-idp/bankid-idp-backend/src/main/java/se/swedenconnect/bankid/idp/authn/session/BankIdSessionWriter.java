package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.bankid.rpapi.types.CompletionData;

import javax.servlet.http.HttpServletRequest;

public interface BankIdSessionWriter {
    void save(HttpServletRequest request, BankIdSessionData data);
    void save(HttpServletRequest request, CollectResponse data);

    void delete(HttpServletRequest request);
}
