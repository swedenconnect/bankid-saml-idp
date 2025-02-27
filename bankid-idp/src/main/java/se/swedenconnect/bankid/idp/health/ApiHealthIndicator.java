/*
 * Copyright 2023-2025 Sweden Connect
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
package se.swedenconnect.bankid.idp.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;

/**
 * Health check for the API.
 *
 * @author Martin Lindström
 * @author Felix Hellman
 */
@Component
@AllArgsConstructor
public class ApiHealthIndicator implements HealthIndicator {

  private final CircuitBreaker circuitBreaker;

  /** {@inheritDoc} */
  @Override
  public Health health() {
    final Health.Builder builder = new Health.Builder();
    if (this.circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED)) {
      builder.up();
    }
    else {
      builder.down();
    }
    return builder.build();
  }

  /** {@inheritDoc} */
  @Override
  public Health getHealth(final boolean includeDetails) {
    return HealthIndicator.super.getHealth(true);
  }
}
