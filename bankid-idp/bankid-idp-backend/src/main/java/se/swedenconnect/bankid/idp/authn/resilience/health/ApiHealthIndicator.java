package se.swedenconnect.bankid.idp.authn.resilience.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ApiHealthIndicator implements HealthIndicator {


    private final CircuitBreaker circuitBreaker;

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        if (circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED)) {
            builder.up();
        } else {
            builder.down();
        }
        return builder.build();
    }

    @Override
    public Health getHealth(boolean includeDetails) {
        return HealthIndicator.super.getHealth(true);
    }
}
