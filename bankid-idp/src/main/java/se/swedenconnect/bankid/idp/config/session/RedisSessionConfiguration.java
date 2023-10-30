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
package se.swedenconnect.bankid.idp.config.session;

import lombok.Setter;
import org.redisson.api.RedissonClient;
import org.redisson.config.BaseConfig;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import se.swedenconnect.bankid.idp.authn.session.RedisSessionDao;
import se.swedenconnect.bankid.idp.authn.session.SessionDao;
import se.swedenconnect.bankid.idp.concurrency.RedisTryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.TryLockRepository;
import se.swedenconnect.bankid.idp.config.RedisTlsProperties;
import se.swedenconnect.bankid.idp.ext.RedisReplayChecker;

import java.io.IOException;
import java.time.Duration;

/**
 * Redis session security configuration.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
@ConditionalOnProperty(value = "bankid.session.module", havingValue = "redis")
@Import({RedissonAutoConfiguration.class, RedisAutoConfiguration.class})
@EnableRedisHttpSession
public class RedisSessionConfiguration {

  /**
   * The replay TTL.
   */
  @Setter
  @Value("${saml.idp.replay-ttl:PT5M}")
  private Duration replayTtl;

  @Bean
  @ConfigurationProperties(prefix = "spring.data.redis.ssl-ext")
  RedisTlsProperties redisTlsProperties() {
    return new RedisTlsProperties();
  }

  @Bean
  @ConditionalOnProperty(value = "spring.data.redis.ssl.enabled", havingValue = "true", matchIfMissing = false)
  RedissonAutoConfigurationCustomizer sslCustomizer(final RedisTlsProperties tlsProperties) {
    return c -> {
      try {
        final BaseConfig<?> config = getConfiguration(c);
        config.setSslEnableEndpointIdentification(tlsProperties.isEnableHostnameVerification());
        if (tlsProperties.getCredential() != null) {
          config
              .setSslKeystore(tlsProperties.getCredential().getResource().getURL())
              .setSslKeystorePassword(tlsProperties.getCredential().getPassword());
        }
        if (tlsProperties.getTrust() != null) {
          config
              .setSslTruststore(tlsProperties.getTrust().getResource().getURL())
              .setSslTruststorePassword(tlsProperties.getTrust().getPassword());
        }
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private BaseConfig<?> getConfiguration(Config config) {
    BaseConfig<?> baseConfig = config.useSingleServer();
    if (config.isSingleConfig()) {
      return RedissonAddressCustomizers.singleServerSslCustomizer.apply(config.useSingleServer());
    }
    if (config.isClusterConfig()) {
      return RedissonAddressCustomizers.clusterServerCustomizer.apply(config.useClusterServers());
    }
    if (config.isSentinelConfig()) {
      throw new IllegalArgumentException("Sentinel Configuration is not implementend");
    }
    return baseConfig;
  }

  @Bean
  TryLockRepository repository(final RedissonClient client) {
    return new RedisTryLockRepository(client);
  }

  @Bean
  SessionDao redisSessionDao(final RedissonClient client) {
    return new RedisSessionDao(client);
  }

  @Bean
  RedisReplayChecker redisReplayChecker(final RedissonClient client) {
    final RedisReplayChecker checker = new RedisReplayChecker(client);
    checker.setReplayCacheExpiration(this.replayTtl);
    return checker;
  }
}
