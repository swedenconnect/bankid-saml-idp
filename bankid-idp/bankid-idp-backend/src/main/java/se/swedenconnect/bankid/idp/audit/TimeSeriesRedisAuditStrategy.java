package se.swedenconnect.bankid.idp.audit;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.api.TimeSeriesEntry;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
public class TimeSeriesRedisAuditStrategy implements RedisAuditStrategy {
  private final RedissonClient client;
  private final AuditEventMapper mapper;
  @Override
  public void add(AuditEvent event) {
    client.getTimeSeries("audit:ts").add(event.getTimestamp().toEpochMilli(), mapper.write(event));
  }

  @Override
  public List<AuditEvent> find(String principal, Instant after, String type) {
    Collection<TimeSeriesEntry<Object, Object>> timeSeries = client.getTimeSeries("audit:ts").entryRange(after.toEpochMilli(), Instant.now().plus(1, ChronoUnit.MINUTES).toEpochMilli());
    Stream<AuditEvent> auditEventStream = timeSeries.stream()
        .map(e -> mapper.read((String) e.getValue()));
    if (Objects.nonNull(principal)) {
      return auditEventStream.filter(event -> principal.equals(event.getPrincipal())).toList();
    }
    return auditEventStream.toList();
  }
}
