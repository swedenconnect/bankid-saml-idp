package se.swedenconnect.bankid.idp.authn.log;

import se.swedenconnect.bankid.idp.rp.RelyingPartyData;

import javax.servlet.http.HttpServletRequest;

public class AuditIdentifierFactory {
  public static AuditIdentifier create(final HttpServletRequest request, final RelyingPartyData data, AuditIdentifier.Type eventType) {
    String id = request.getSession().getId();
    return new AuditIdentifier(id, data.getId(), data.getEntityIds(), eventType);
  }
}
