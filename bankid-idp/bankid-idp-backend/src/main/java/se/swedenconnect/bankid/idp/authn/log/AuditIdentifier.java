package se.swedenconnect.bankid.idp.authn.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Data
@ToString
public class AuditIdentifier {

  public enum Type {
    SUCCESS,
    FAILURE,
    START,
    COLLECT
  }

  private final String sessionId;
  private final String relayingParty;
  private final List<String> entityIds;
  private final Type eventType;
}
