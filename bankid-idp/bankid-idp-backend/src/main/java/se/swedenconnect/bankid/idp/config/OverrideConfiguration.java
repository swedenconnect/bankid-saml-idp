package se.swedenconnect.bankid.idp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import se.swedenconnect.bankid.idp.authn.api.overrides.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Supplier;

@Configuration
public class OverrideConfiguration {

  @Bean
  public OverrideService overrideService(List<Supplier<CssOverride>> cssOverrides, List<Supplier<MessageOverride>> messageOverrides, List<Supplier<ContentOverride>> contentOverrides, OverrideFileLoader fileLoader) {
    return new OverrideService(cssOverrides, messageOverrides, contentOverrides, fileLoader);
  }

  @Bean
  public OverrideFileLoader overrideFileLoader(OverrideProperties properties, ObjectMapper mapper) {
    return new OverrideFileLoader(properties, mapper);
  }

  @Bean
  @ConfigurationProperties(prefix = "bankid.override")
  public OverrideProperties overrideProperties() {
    return new OverrideProperties();
  }
}
