package se.swedenconnect.bankid.idp.integration;

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
}
