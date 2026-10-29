package com.crm.hibernate.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics implements MeterBinder {

    private Counter healthCheckCounter;
    private Counter authenticationAttemptCounter;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.healthCheckCounter = Counter.builder("gym.health.checks")
                .description("Total number of health checks performed")
                .register(registry);

        this.authenticationAttemptCounter = Counter.builder("gym.authentication.attempts")
                .description("Total number of authentication attempts")
                .register(registry);

        registry.counter("gym.application.events", "type", "started").increment();
    }

    public void recordHealthCheck() {
        if (healthCheckCounter != null) {
            healthCheckCounter.increment();
        }
    }

    public void recordAuthenticationAttempt() {
        if (authenticationAttemptCounter != null) {
            authenticationAttemptCounter.increment();
        }
    }
}