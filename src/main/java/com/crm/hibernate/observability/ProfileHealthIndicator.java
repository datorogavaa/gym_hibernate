package com.crm.hibernate.observability;

import java.util.Arrays;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("gymEnvironment")
public class ProfileHealthIndicator implements HealthIndicator {
    private final Environment environment;

    public ProfileHealthIndicator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Health health() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            profiles = environment.getDefaultProfiles();
        }
        if (profiles.length == 0) {
            return Health.down().withDetail("reason", "no active profile").build();
        }
        return Health.up().withDetail("profiles", Arrays.asList(profiles)).build();
    }
}
