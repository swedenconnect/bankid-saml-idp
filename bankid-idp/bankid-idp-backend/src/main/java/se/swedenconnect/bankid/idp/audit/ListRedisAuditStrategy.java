package se.swedenconnect.bankid.idp.audit;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class ListRedisAuditStrategy implements RedisAuditStrategy {

  private final RedissonClient client;

  private final AuditEventMapper mapper;

  @Override
  public void add(AuditEvent event) {
    client.getList("audit:list").add(event);
  }

  @Override
  public List<AuditEvent> find(String principal, Instant after, String type) {
    return client.getList("audit:list").stream()
        .map(String.class::cast)
        .map(mapper::read)
        .toList();
  }
}
