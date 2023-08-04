package se.swedenconnect.bankid.idp.authn.error;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.swedenconnect.bankid.idp.authn.UserErrorProperties;

@Configuration
public class UserErrorConfiguration {
  @Bean
  @ConfigurationProperties(prefix = "user.error")
  public UserErrorProperties userErrorProperties() {
    return new UserErrorProperties();
  }
}
