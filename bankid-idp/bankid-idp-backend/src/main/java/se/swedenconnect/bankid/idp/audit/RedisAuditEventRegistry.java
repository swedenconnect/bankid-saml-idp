package se.swedenconnect.bankid.idp.audit;

import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class RedisAuditEventRegistry implements AuditEventRepository {

  private final RedisAuditStrategy redis;

  @Override
  public void add(AuditEvent event) {
    redis.add (event);
  }

  @Override
  public List<AuditEvent> find(String principal, Instant after, String type) {
    return redis.find(principal, after, type);
  }
}
