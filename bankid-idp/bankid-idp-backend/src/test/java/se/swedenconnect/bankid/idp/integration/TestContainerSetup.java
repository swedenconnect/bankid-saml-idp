package se.swedenconnect.bankid.idp.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainerSetup {
  public static final GenericContainer redis;

  static {
    redis =  new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
        .withNetworkAliases("redis")
        .withExposedPorts(6379)
            .withCommand("--requirepass supersecret");
    redis.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.ssl", () -> false);
    registry.add("spring.redis.host", redis::getHost);
    registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
  }
}
