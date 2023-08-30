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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import se.swedenconnect.bankid.idp.authn.session.ServletSessionDao;
import se.swedenconnect.bankid.idp.authn.session.SessionDao;
import se.swedenconnect.bankid.idp.concurrency.InMemoryTryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.TryLockRepository;
import se.swedenconnect.opensaml.saml2.response.replay.InMemoryReplayChecker;
import se.swedenconnect.spring.saml.idp.authnrequest.validation.AbstractMessageReplayChecker;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "session.module", havingValue = "memory", matchIfMissing = true)
public class InMemorySessionConfiguration {

  // This is meant for development, can cause issues if deployed in a production environment
  @Bean
  public TryLockRepository inMemoryLockRepository() {
    log.warn("Starting application with in memory repository, this is not meant for production use");
    return new InMemoryTryLockRepository();
  }

  @Bean
  public SessionDao springSessionBankidSessions() {
    log.warn("Starting application with in memory sessions, this is not meant for production use");
    return new ServletSessionDao();
  }

  @Bean
  public InMemoryReplayChecker inMemoryReplayChecker() {
    return new InMemoryReplayChecker();
  }
}
