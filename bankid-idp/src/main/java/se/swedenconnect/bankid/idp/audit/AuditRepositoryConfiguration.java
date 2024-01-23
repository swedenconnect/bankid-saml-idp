/*
 * Copyright 2023-2024 Sweden Connect
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;
import se.swedenconnect.spring.saml.idp.audit.Saml2AuditEvents;
import se.swedenconnect.spring.saml.idp.audit.repository.MemoryBasedAuditEventRepository;
import se.swedenconnect.spring.saml.idp.audit.repository.RedisListAuditEventRepository;
import se.swedenconnect.spring.saml.idp.audit.repository.RedissonTimeSeriesAuditEventRepository;
import se.swedenconnect.spring.saml.idp.autoconfigure.audit.AuditEventRepositoryFactory;
import se.swedenconnect.spring.saml.idp.autoconfigure.audit.AuditRepositoryConfigurationProperties;

/**
 * Configuration for setting up the audit event repositories.
 * <p>
 * The SAML IdP starter has all the actual configuration. This class maps deprecated settings to new new configuration
 * properties.
 * </p>
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@EnableConfigurationProperties({ BankIdConfigurationProperties.class, AuditRepositoryConfigurationProperties.class })
@Slf4j
public class AuditRepositoryConfiguration {

  /**
   * The events that we support by default.
   */
  public static List<String> DEFAULT_SUPPORTED_EVENTS = Stream.concat(
      Arrays.stream(Saml2AuditEvents.values())
          .map(Saml2AuditEvents::getTypeName)
          .toList().stream(),
      Arrays.stream(BankIdAuditEventTypes.values())
          .map(BankIdAuditEventTypes::getTypeName)
          .toList().stream())
      .toList();

  /**
   * Audit configuration properties (deprecated).
   */
  private final BankIdConfigurationProperties.AuditConfiguration deprecatedConfig;

  /** Audit configuration properties. */
  private final AuditRepositoryConfigurationProperties config;

  /**
   * Constructor.
   *
   * @param properties the BankID configuration properties
   */
  public AuditRepositoryConfiguration(final BankIdConfigurationProperties properties,
      final AuditRepositoryConfigurationProperties auditProperties) {
    this.deprecatedConfig = Objects.requireNonNull(properties, "properties must not be null").getAudit();
    this.config = Objects.requireNonNull(auditProperties, "auditProperties must not be null");
  }

  @Bean
  AuditConfigurationModifier auditConfigurationModifier() throws Exception {
    return new AuditConfigurationModifier(this.deprecatedConfig, this.config);
  }

  /**
   * Given the deprecated {@code BankIdConfigurationProperties.AuditConfiguration} its settings are mapped to
   * {@link AuditRepositoryConfigurationProperties}.
   */
  static class AuditConfigurationModifier {

    /**
     * Constructor performing the mapping.
     *
     * @param deprecatedProperties the deprecated audit properties
     * @param auditProperties the actual audit properties
     * @throws Exception for errors
     */
    public AuditConfigurationModifier(
        final BankIdConfigurationProperties.AuditConfiguration deprecatedProperties,
        final AuditRepositoryConfigurationProperties auditProperties) throws Exception {

      // File
      //
      if (deprecatedProperties.getLogFile() != null) {
        if (auditProperties.getFile() == null) {
          auditProperties.setFile(new AuditRepositoryConfigurationProperties.FileRepository());
          auditProperties.getFile().setLogFile(deprecatedProperties.getLogFile());
          log.warn("DEPRECATION WARNING. Mapping setting bankid.audit.log-file to saml.idp.audit.file.log-file");
          auditProperties.getFile().afterPropertiesSet();
        }
        else {
          throw new BeanCreationException("Settings bankid.audit.log-file and saml.idp.audit.file.log-file are set. "
              + "Remove bankid.audit.*");
        }
      }

      // Included events
      //
      if (deprecatedProperties.getSupportedEvents() != null && !deprecatedProperties.getSupportedEvents().isEmpty()) {
        if (auditProperties.getIncludeEvents().isEmpty()) {
          auditProperties.getIncludeEvents().addAll(deprecatedProperties.getSupportedEvents());
          log.warn(
              "DEPRECATION WARNING. Mapping configured values from bankid.audit.supported-events to saml.idp.audit.include-events");
        }
      }

      // Repository
      //
      if (deprecatedProperties.getRepository().equals("memory") && auditProperties.getInMemory() == null) {
        auditProperties.setInMemory(new AuditRepositoryConfigurationProperties.InMemoryRepository());
        auditProperties.afterPropertiesSet();
        log.info("DEPRECATION WARNING. Mapping bankid.audit.repository=memory to saml.idp.audit.in-memory.capacity={}",
            MemoryBasedAuditEventRepository.DEFAULT_CAPACITY);
      }
      if (deprecatedProperties.getRepository().equals("redislist")
          || deprecatedProperties.getRepository().equals("redistimeseries")) {
        if (auditProperties.getRedis() == null) {
          auditProperties.setRedis(new AuditRepositoryConfigurationProperties.RedisRepository());
          auditProperties.getRedis().setType(deprecatedProperties.getRepository().equals("redislist")
              ? "list"
              : "timeseries");
          auditProperties.afterPropertiesSet();
          log.warn(
              "DEPRECATION WARNING. Mapping bankid.audit.repository={} to saml.idp.audit.redis.type={} and saml.idp.audit.redis.name={}",
              deprecatedProperties.getRepository(), auditProperties.getRedis().getType(),
              auditProperties.getRedis().getName());
        }
        else {
          throw new BeanCreationException(String.format(
              "Settings bankid.audit.repository=%s and saml.idp.audit.redis.* are set. "
                  + "Remove bankid.audit.*",
              deprecatedProperties.getRepository()));
        }
      }

    }

  }

  // To support the deprecated "bankid.audit.repository" setting ...
  @Configuration
  @ConditionalOnProperty(value = "bankid.audit.repository", havingValue = "redistimeseries", matchIfMissing = false)
  public class RedissonAuditRepositoryConfiguration {

    /**
     * Creates an {@link AuditEventRepositoryFactory} that creates a
     * {@link RedissonTimeSeriesAuditEventRepository} bean.
     *
     * @param redissonClient the Redisson client bean
     * @return an {@link AuditEventRepositoryFactory}
     */
    @Bean
    AuditEventRepositoryFactory redisTimeseriesRepository(final RedissonClient redissonClient) {
      return (name, mapper, filter) -> new RedissonTimeSeriesAuditEventRepository(redissonClient, name, mapper, filter);
    }

  }

  // To support the deprecated "bankid.audit.repository" setting ...
  @Configuration
  @ConditionalOnProperty(value = "bankid.audit.repository", havingValue = "redislist", matchIfMissing = false)
  public class RedisAuditRepositoryAutoConfiguration {

    /**
     * Creates an {@link AuditEventRepositoryFactory} that creates a {@link RedisListAuditEventRepository} bean.
     *
     * @param redisTemplate the Redis template
     * @return an {@link AuditEventRepositoryFactory}
     */
    @Bean
    AuditEventRepositoryFactory redisListRepository(final StringRedisTemplate redisTemplate) {
      return (name, mapper, filter) -> new RedisListAuditEventRepository(redisTemplate, name, mapper, filter);
    }

  }

}
