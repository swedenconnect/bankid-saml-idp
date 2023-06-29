package se.swedenconnect.bankid.idp.config.session;

import org.redisson.api.RedissonClient;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import se.swedenconnect.bankid.idp.authn.session.RedisSessionDao;
import se.swedenconnect.bankid.idp.authn.session.SessionDao;
import se.swedenconnect.bankid.idp.concurrency.TryLockRepository;
import se.swedenconnect.bankid.idp.concurrency.RedisTryLockRepository;
import se.swedenconnect.bankid.idp.config.RedisSecurityProperties;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(value = "session.module", havingValue = "redis")
@Import({RedissonAutoConfiguration.class, RedisAutoConfiguration.class})
@EnableRedisHttpSession
public class RedisSessionConfiguration {
  private final ResourceLoader loader = new DefaultResourceLoader();

  @Bean
  @ConfigurationProperties(prefix = "spring.redis.tls")
  public RedisSecurityProperties redisSecurityProperties() {
    return new RedisSecurityProperties();
  }
  @Bean
  public RedissonAutoConfigurationCustomizer sslCustomizer(final RedisSecurityProperties properties) {
    final Resource keystore = loader.getResource(properties.getP12KeyStorePath());
    return c -> {
      try {
        final SingleServerConfig singleServerConfig = c.useSingleServer()
            .setSslKeystore(keystore.getURL())
            .setSslKeystorePassword(properties.getP12KeyStorePassword());
        singleServerConfig.setSslEnableEndpointIdentification(properties.getEnableHostnameVerification());
        if (properties.getEnableHostnameVerification()) {
          final Resource truststore = loader.getResource(properties.getP12TrustStorePath());
          singleServerConfig
              .setSslTruststore(truststore.getURL())
              .setSslTruststorePassword(properties.getP12TrustStorePassword());
        }
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Bean
  public TryLockRepository repository(final RedissonClient client) {
    return new RedisTryLockRepository(client);
  }

  @Bean
  public SessionDao redisSessionDao(final RedissonClient client) {
    return new RedisSessionDao(client);
  }
}
