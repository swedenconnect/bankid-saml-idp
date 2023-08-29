package se.swedenconnect.bankid.idp.authn.log;

import com.google.common.collect.ImmutableMap;
import se.swedenconnect.bankid.idp.authn.service.PollRequest;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessionData;
import se.swedenconnect.bankid.idp.rp.RelyingPartyData;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;
import se.swedenconnect.spring.saml.idp.authentication.provider.external.RedirectForAuthenticationToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AuditIdentifierFactory {

  public static Map<String, Object> createAuditIdentifier(final RelyingPartyData data, final RedirectForAuthenticationToken token, final BankIdSessionData sessionData) {
    String entityId = token.getAuthnInputToken().getAuthnRequestToken().getAuthnRequest().getIssuer().getValue();
    String authRequestId = token.getAuthnInputToken().getAuthnRequestToken().getAuthnRequest().getID();
    return Map.of(
        "rp", data.getId(),
        "entityId", entityId,
        "samlId", authRequestId,
        "orderRef", sessionData.getOrderReference(),
        "timestamp", Instant.now(),
        "operation", sessionData.getOperation().getValue()
    );
  }

  public static Map<String, Object> createInitialAuditIdentifier(final PollRequest pollRequest, final RedirectForAuthenticationToken token) {
    String entityId = token.getAuthnInputToken().getAuthnRequestToken().getAuthnRequest().getIssuer().getValue();
    String authRequestId = token.getAuthnInputToken().getAuthnRequestToken().getAuthnRequest().getID();
    return Map.of(
        "rp", pollRequest.getRelyingPartyData().getId(),
        "entityId", entityId,
        "samlId", authRequestId,
        "timestamp", Instant.now(),
        "operation", pollRequest.getContext().getOperation()
    );
  }

  public static Map<String, Object> createCompleteAuditIdentifier(final RelyingPartyData data, final RedirectForAuthenticationToken token, final BankIdSessionData sessionData, final CollectResponse collectResponse) {
    HashMap<String, Object> auditIdentifier = new HashMap<>(createAuditIdentifier(data, token, sessionData));
    auditIdentifier.put("userId", collectResponse.getCompletionData().getUser().getPersonalNumber());
    return ImmutableMap.copyOf(auditIdentifier);
  }
}
