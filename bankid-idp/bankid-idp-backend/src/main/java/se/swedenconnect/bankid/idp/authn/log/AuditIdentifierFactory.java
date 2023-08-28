package se.swedenconnect.bankid.idp.authn.log;

import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AuditIdentifierFactory {

  public static Map<String, Object> createAuditIdentifier(final HttpServletRequest request, final RelyingPartyData data, final String authNRequestId, final BankIdSessionData sessionData) {
    final String id = request.getSession().getId();
    return Map.of(
        "sessionId", id,
        "rp", data.getId(),
        "entityId",data.getEntityIds(),
        "samlId", authNRequestId,
        "orderRef", sessionData.getOrderReference(),
        "timestamp", Instant.now(),
        "operation", sessionData.getOperation().getValue()
    );
  }

  public static Map<String, Object> createCompleteAuditIdentifier(final HttpServletRequest request, final RelyingPartyData data, final String authNRequestId, final BankIdSessionData sessionData, final CollectResponse collectResponse) {
    HashMap<String, Object> auditIdentifier = new HashMap<>(createAuditIdentifier(request, data, authNRequestId, sessionData));
    auditIdentifier.put("userId", collectResponse.getCompletionData().getUser().getPersonalNumber());
    return auditIdentifier;
  }
}
