package se.swedenconnect.bankid.idp.authn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"unittest"})
class BankIdAuthenticationControllerTest {

  @Autowired
  BankIdAuthenticationController controller;

  @MockBean
  BankIdAuthenticationProvider provider;

  public static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
      .withEnv("REDIS_ARGS", "--requirepass supersecret")
      .withExposedPorts(6379);


  static {
    redis.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
    registry.add("spring.redis.password", () -> null);
    registry.add("spring.redis.username", () -> null);
  }



  @Test
  void testTest() {
    BankIdAuthenticationProvider provider1 = controller.getProvider();
    System.out.println("test");
  }

}