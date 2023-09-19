package se.swedenconnect.bankid.idp.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import se.swedenconnect.bankid.idp.config.BankIdConfigurationProperties;

import java.util.function.Function;

@Configuration
public class IntegrationConfiguration {

  @Bean
  @Primary
  public Function<BankIdConfigurationProperties.RelyingPartyConfiguration, WebClient> testWebClientFactory() {
    return rp -> {
      return WebClient.builder().baseUrl("http://localhost:9000").build();
    };
  }
}
