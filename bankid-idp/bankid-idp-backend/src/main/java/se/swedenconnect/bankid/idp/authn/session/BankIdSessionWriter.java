package se.swedenconnect.bankid.idp.authn.session;

import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;

public interface BankIdSessionWriter {
  void save(HttpServletRequest request, BankIdSessionData data);

  void save(HttpServletRequest request, CollectResponse data);

  void delete(HttpServletRequest request);

  void save(HttpServletRequest request, PreviousDeviceSelection previousDeviceSelection);
}
