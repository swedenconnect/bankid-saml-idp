package se.swedenconnect.bankid.idp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuditRepositoryConfiguration {

  @Bean
  public RedisAuditStrategy redisListAuditStrategy(final RedissonClient client, final AuditEventMapper mapper) {
    return new ListRedisAuditStrategy(client, mapper);
  }

  @Bean
  @Primary
  public RedisAuditStrategy redisTimeSeriesAuditStrategy(final RedissonClient client, final AuditEventMapper mapper) {
    return new TimeSeriesRedisAuditStrategy(client, mapper);
  }

  @Bean
  public RedisAuditEventRegistry redisAuditEventRegistry(RedisAuditStrategy strategy) {
    return new RedisAuditEventRegistry(strategy);
  }

  @Bean
  public AuditEventMapper auditEventMapper(ObjectMapper mapper) {
    return new AuditEventMapper(mapper);
  }
}
