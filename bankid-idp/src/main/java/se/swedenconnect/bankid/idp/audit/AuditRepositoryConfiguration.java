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

import java.io.IOException;
import java.util.Objects;

import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;

/**
 * Configuration for setting up the audit event repositories.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@EnableConfigurationProperties(BankIdConfigurationProperties.class)
public class AuditRepositoryConfiguration {

  /**
   * Audit configuration properties.
   */
  private final BankIdConfigurationProperties.AuditConfiguration config;

  /**
   * Constructor.
   *
   * @param properties the BankID configuration properties
   */
  public AuditRepositoryConfiguration(final BankIdConfigurationProperties properties) {
    this.config = Objects.requireNonNull(properties, "properties must not be null").getAudit();
  }

  @Bean
  @ConditionalOnProperty(value = "bankid.audit.repository", havingValue = "memory", matchIfMissing = true)
  AuditEventRepository inMemoryAuditRepository(final AuditEventMapper mapper) throws IOException {
    return new MemoryBasedAuditEventRepository(this.config.getLogFile(), mapper, this.config.getSupportedEvents());
  }

  @Bean
  @ConditionalOnProperty(value = "bankid.audit.repository", havingValue = "redislist", matchIfMissing = false)
  AuditEventRepository redisListAuditRepository(final RedissonClient client, final AuditEventMapper mapper)
      throws IOException {
    return new RedisListAuditEventRepository(client, this.config.getLogFile(), mapper,
        this.config.getSupportedEvents());
  }

  @Bean
  @ConditionalOnProperty(value = "bankid.audit.repository", havingValue = "redistimeseries", matchIfMissing = false)
  AuditEventRepository redisTimeSeriesAuditStrategy(final RedissonClient client, final AuditEventMapper mapper)
      throws IOException {
    return new RedisTimeSeriesAuditEventRepository(client, this.config.getLogFile(), mapper,
        this.config.getSupportedEvents());
  }

  @Bean
  AuditEventMapper auditEventMapper(final ObjectMapper mapper) {
    return new AuditEventMapper(mapper);
  }
}
