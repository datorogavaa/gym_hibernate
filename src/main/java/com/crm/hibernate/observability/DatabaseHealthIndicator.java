package com.crm.hibernate.observability;

import javax.sql.DataSource;
import java.sql.Connection;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("gymDatabase")
public class DatabaseHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;
    private final GymMetrics metrics;

    public DatabaseHealthIndicator(DataSource dataSource, GymMetrics metrics) {
        this.dataSource = dataSource;
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        metrics.recordHealthCheck();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return Health.up().withDetail("database", connection.getMetaData().getDatabaseProductName()).build();
            }
            return Health.down().withDetail("database", "connection is not valid").build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
