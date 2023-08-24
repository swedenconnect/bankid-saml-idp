package se.swedenconnect.bankid.idp.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.time.Instant;
import java.util.List;

public interface RedisAuditStrategy {
  void add(AuditEvent auditEvent);

  List<AuditEvent> find(String principal, Instant after, String type);

}
