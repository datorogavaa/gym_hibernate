package com.crm.hibernate.observability;

import java.util.concurrent.atomic.AtomicLong;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics implements MeterBinder {
    private final AtomicLong healthChecks = new AtomicLong();
    private final AtomicLong authenticationAttempts = new AtomicLong();

    @Override
    public void bindTo(MeterRegistry registry) {
        registry.gauge("gym.health.checks", healthChecks);
        registry.gauge("gym.authentication.attempts", authenticationAttempts);
        registry.counter("gym.application.events", "type", "started").increment();
    }

    public void recordHealthCheck() {
        healthChecks.incrementAndGet();
    }

    public void recordAuthenticationAttempt() {
        authenticationAttempts.incrementAndGet();
    }
}
