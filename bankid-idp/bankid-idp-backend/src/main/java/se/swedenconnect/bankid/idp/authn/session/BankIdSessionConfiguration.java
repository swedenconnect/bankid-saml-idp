package se.swedenconnect.bankid.idp.authn.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankIdSessionConfiguration {
    @Bean
    public SpringSessionBankidSessions springSessionBankidSessions() {
        return new SpringSessionBankidSessions();
    }
}
