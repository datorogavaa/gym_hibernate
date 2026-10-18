package com.crm.hibernate.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymMetricsTest {
    @Test
    void registersCustomMetrics() {
        var registry = new SimpleMeterRegistry();
        var metrics = new GymMetrics();

        metrics.bindTo(registry);
        metrics.recordAuthenticationAttempt();
        metrics.recordHealthCheck();

        assertEquals(1.0, registry.get("gym.authentication.attempts").gauge().value());
        assertEquals(1.0, registry.get("gym.health.checks").gauge().value());
        assertEquals(1.0, registry.get("gym.application.events").counter().count());
    }
}
