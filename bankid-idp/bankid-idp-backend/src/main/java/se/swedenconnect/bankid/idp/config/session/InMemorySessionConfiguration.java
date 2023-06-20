package se.swedenconnect.bankid.idp.config.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.swedenconnect.bankid.idp.authn.session.SpringSessionBankidSessions;
import se.swedenconnect.bankid.idp.concurrency.InMemoryTryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.TryLockRepository;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "session.module", havingValue = "memory", matchIfMissing = true)
public class InMemorySessionConfiguration {

  // TODO: 2023-06-16 This is meant for development, can cause issues if deployed in a production environment
  @Bean
  public TryLockRepository inMemoryLockRepository() {
    log.warn("Starting application with in memory repository, this is not meant for production use");
    return new InMemoryTryLockRepository();
  }

  @Bean
  public SpringSessionBankidSessions springSessionBankidSessions() {
    log.warn("Starting application with in memory sessions, this is not meant for production use");
    return new SpringSessionBankidSessions();
  }
}
