package se.swedenconnect.bankid.idp.config;

import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
public class RedisConfiguration {
  private final ResourceLoader loader = new DefaultResourceLoader();

  @Bean
  @ConfigurationProperties(prefix = "spring.redis.tls")
  public RedisSecurityProperties redisSecurityProperties() {
    return new RedisSecurityProperties();
  }

  @Bean
  public RedissonAutoConfigurationCustomizer sslCustomizer(RedisSecurityProperties properties) {
    Resource keystore = loader.getResource(properties.getP12KeyStorePath());
    return c -> {
      try {
        SingleServerConfig singleServerConfig = c.useSingleServer()
            .setSslKeystore(keystore.getURL())
            .setSslKeystorePassword(properties.getP12KeyStorePassword());
        singleServerConfig.setSslEnableEndpointIdentification(properties.getEnableHostnameVerification());
        if (properties.getEnableHostnameVerification()) {
          Resource truststore = loader.getResource(properties.getP12TrustStorePath());
          singleServerConfig
              .setSslTruststore(truststore.getURL())
              .setSslTruststorePassword(properties.getP12TrustStorePassword());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }
}
