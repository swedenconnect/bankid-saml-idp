package se.swedenconnect.bankid.idp.config.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.swedenconnect.bankid.idp.authn.session.BankIdSessions;
import se.swedenconnect.bankid.idp.authn.session.SessionDao;
import se.swedenconnect.bankid.idp.authn.session.SpringSessionDao;
import se.swedenconnect.bankid.idp.concurrency.InMemoryTryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.TryLockRepository;

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
    return new SpringSessionDao();
  }
}
