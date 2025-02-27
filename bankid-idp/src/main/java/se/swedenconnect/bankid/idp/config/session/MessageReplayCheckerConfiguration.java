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

import org.opensaml.storage.ReplayCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import se.swedenconnect.opensaml.saml2.response.replay.MessageReplayChecker;
import se.swedenconnect.spring.saml.idp.authnrequest.validation.replay.InMemoryReplayCache;
import se.swedenconnect.spring.saml.idp.authnrequest.validation.replay.RedisReplayCache;

/**
 * For setting up the {@link MessageReplayChecker}.
 * <p>
 * Note: This is actually done by the Spring SAML IdP starter, but we also want to support our deprecated session
 * settings.
 * </p>
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
public class MessageReplayCheckerConfiguration {

  @ConditionalOnProperty(value = "saml.idp.replay.type")
  @Bean("correctReplaySettingsAssigned")
  String correctReplaySettingsAssigned() {
    return "correctReplaySettingsAssigned";
  }

  @ConditionalOnMissingBean(value = ReplayCache.class, name = "correctReplaySettingsAssigned")
  @ConditionalOnProperty(value = "bankid.session.module", havingValue = "memory", matchIfMissing = false)
  @Bean
  ReplayCache inMemoryReplayCache() {
    return new InMemoryReplayCache();
  }

  @ConditionalOnMissingBean(value = ReplayCache.class, name = "correctReplaySettingsAssigned")
  @ConditionalOnProperty(value = "bankid.session.module", havingValue = "redis", matchIfMissing = false)
  @Bean
  ReplayCache redisReplayCache(final StringRedisTemplate redisTemplate) {
    return new RedisReplayCache(redisTemplate);
  }

}
