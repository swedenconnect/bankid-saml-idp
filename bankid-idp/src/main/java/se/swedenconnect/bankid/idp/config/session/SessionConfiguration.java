/*
 * Copyright 2023-2025 Sweden Connect
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

import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import se.swedenconnect.bankid.idp.authn.session.RedisSessionDao;
import se.swedenconnect.bankid.idp.authn.session.ServletSessionDao;
import se.swedenconnect.bankid.idp.authn.session.SessionDao;
import se.swedenconnect.bankid.idp.concurrency.InMemoryTryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.RedisTryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.TryLockRepository;
import se.swedenconnect.bankid.idp.config.session.SessionDeprecationConfiguration.SessionPropertyModifier;
import se.swedenconnect.spring.saml.idp.autoconfigure.session.MemorySessionAutoConfiguration;

/**
 * Configuration for session handling.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
public class SessionConfiguration {

  /** To ensure deprecated fixes have been applied. */
  @Autowired
  SessionPropertyModifier _dummy;

  @Configuration
  @ConditionalOnProperty(name = "saml.idp.session.module", havingValue = "redis")
  public static class ActualRedisSessionConfiguration {

    @ConditionalOnMissingBean
    @Bean("bankidTryLockRepository")
    TryLockRepository repository(final RedissonClient client) {
      return new RedisTryLockRepository(client);
    }

    @ConditionalOnMissingBean
    @Bean("bankidSessionDao")
    SessionDao redisSessionDao(final RedissonClient client) {
      return new RedisSessionDao(client);
    }

  }

  @Configuration
  @ConditionalOnProperty(name = "saml.idp.session.module", havingValue = "memory")
  public static class ActualInMemorySessionConfiguration {

    @ConditionalOnMissingBean
    @Bean("bankidTryLockRepository")
    TryLockRepository inMemoryLockRepository() {
      return new InMemoryTryLockRepository();
    }

    @ConditionalOnMissingBean
    @Bean("bankidSessionDao")
    SessionDao springSessionBankidSessions() {
      return new ServletSessionDao();
    }

  }

  @Configuration
  @ConditionalOnProperty(name = "bankid.session.module", havingValue = "redis")
  @EnableRedisHttpSession
  public static class DeprecatedRedisSessionConfiguration {

    @ConditionalOnMissingBean
    @Bean("bankidTryLockRepository")
    TryLockRepository repository(final RedissonClient client) {
      return new RedisTryLockRepository(client);
    }

    @ConditionalOnMissingBean
    @Bean("bankidSessionDao")
    SessionDao redisSessionDao(final RedissonClient client) {
      return new RedisSessionDao(client);
    }

  }

  @Configuration
  @ConditionalOnProperty(name = "bankid.session.module", havingValue = "memory")
  @EnableSpringHttpSession
  @EnableScheduling
  @ImportAutoConfiguration(exclude = { RedissonAutoConfigurationV2.class, RedisAutoConfiguration.class })
  public static class DeprecatedInMemorySessionConfiguration extends MemorySessionAutoConfiguration {

    public DeprecatedInMemorySessionConfiguration(final ServerProperties serverProperties,
        final SessionProperties sessionProperties) {
      super(serverProperties, sessionProperties);
    }

    @ConditionalOnMissingBean
    @Bean
    TryLockRepository inMemoryLockRepository() {
      return new InMemoryTryLockRepository();
    }

    @ConditionalOnMissingBean
    @Bean
    SessionDao springSessionBankidSessions() {
      return new ServletSessionDao();
    }

  }

}
