/*
 * Copyright 2023 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for setting up the audit event repositories
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
public class AuditRepositoryConfiguration {

  @Bean
  @ConditionalOnProperty(value = "bankid.audit.module", havingValue = "memory", matchIfMissing = false)
  public AuditEventRepository inMemoryAuditRepository() {
    return new InMemoryAuditEventRepository();
  }


  @Bean
  @ConditionalOnProperty(value = "bankid.audit.module", havingValue = "redislist", matchIfMissing = false)
  public AuditEventRepository redisListAuditRepository(final RedissonClient client, final AuditEventMapper mapper) {
    return new ListAuditRepository(client, mapper);
  }

  @Bean
  @ConditionalOnProperty(value = "bankid.audit.module", havingValue = "redistimeseries", matchIfMissing = true)
  public AuditEventRepository redisTimeSeriesAuditStrategy(final RedissonClient client, final AuditEventMapper mapper) {
    return new TimeSeriesAuditRepository(client, mapper);
  }

  @Bean
  public AuditEventMapper auditEventMapper(final ObjectMapper mapper) {
    return new AuditEventMapper(mapper);
  }
}
