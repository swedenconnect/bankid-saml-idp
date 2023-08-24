package se.swedenconnect.bankid.idp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuditRepositoryConfiguration {

  @Bean
  public AuditEventRepository redisListAuditStrategy(final RedissonClient client, final AuditEventMapper mapper) {
    return new ListAuditStrategy(client, mapper);
  }

  @Bean
  @Primary
  public AuditEventRepository redisTimeSeriesAuditStrategy(final RedissonClient client, final AuditEventMapper mapper) {
    return new TimeSeriesAuditStrategy(client, mapper);
  }

  @Bean
  public AuditEventMapper auditEventMapper(ObjectMapper mapper) {
    return new AuditEventMapper(mapper);
  }
}
