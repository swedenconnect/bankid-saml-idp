package se.swedenconnect.bankid.idp.authn.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfiguration {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(MeterRegistry registry) {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(registry);
        return circuitBreakerRegistry;
    }
    @Bean
    public CircuitBreaker circuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker bankid = registry.circuitBreaker("bankid");
        bankid.transitionToForcedOpenState();
        return bankid;
    }
}
