package com.crm.hibernate.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private GymMetrics metrics;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseHealthIndicator(dataSource, metrics);
    }

    @Test
    void health_WhenDatabaseIsValid_ReturnsHealthUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        verify(metrics).recordHealthCheck();
        verify(connection).close(); // Ensures try-with-resources closes connection
    }

    @Test
    void health_WhenConnectionIsNotValid_ReturnsHealthDown() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(false);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("connection is not valid", health.getDetails().get("database"));
        verify(metrics).recordHealthCheck();
        verify(connection).close();
    }

    @Test
    void health_WhenGetConnectionThrowsException_ReturnsHealthDownWithException() throws Exception {
        SQLException expectedException = new SQLException("Database connection refused");
        when(dataSource.getConnection()).thenThrow(expectedException);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertNotNull(health.getDetails().get("error"));
        verify(metrics).recordHealthCheck();
    }
}