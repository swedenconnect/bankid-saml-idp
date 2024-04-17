/*
 * Copyright 2023-2024 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.bankid.idp.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import se.swedenconnect.bankid.rpapi.service.impl.BankIdServerException;

/**
 * Configuration for setting up the Resilience4j beans.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Configuration
public class ResilienceConfiguration {

  @Bean
  public CircuitBreakerConfig circuitBreakerConfig() {
    return new CircuitBreakerConfig.Builder()
        .recordException(e -> e instanceof BankIdServerException)
        .waitDurationInOpenState(Duration.ofMillis(1))
        .slidingWindowSize(10)
        .minimumNumberOfCalls(10)
        .enableAutomaticTransitionFromOpenToHalfOpen()
        .permittedNumberOfCallsInHalfOpenState(10)
        .failureRateThreshold(80)
        .build();
  }

  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry(final CircuitBreakerConfig config, final MeterRegistry registry) {
    final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(config);
    TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(registry);
    return circuitBreakerRegistry;
  }

  @Bean
  public CircuitBreaker circuitBreaker(final CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("bankid");
  }
}
