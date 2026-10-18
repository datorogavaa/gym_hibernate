package com.crm.hibernate.observability;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileHealthIndicatorTest {
    @Test
    void reportsActiveProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");

        var health = new ProfileHealthIndicator(environment).health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals(java.util.List.of("dev"), health.getDetails().get("profiles"));
    }
}
