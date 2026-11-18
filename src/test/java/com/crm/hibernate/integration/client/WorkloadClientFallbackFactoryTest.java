package com.crm.hibernate.integration.client;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkloadClientFallbackFactoryTest {

    private final WorkloadClient client =
            new WorkloadClientFallbackFactory().create(new RuntimeException("offline"));

    @Test
    void getWorkloadReturnsFallbackResponse() {
        Map<String, Object> response = client.getWorkload("bob.jones");

        assertEquals("bob.jones", response.get("trainerUsername"));
        assertEquals("Workload service unavailable", response.get("error"));
    }

    @Test
    void getMonthlyHoursReturnsZeroFallbackResponse() {
        Map<String, Object> response = client.getMonthlyHours("bob.jones", 2026, 11);

        assertEquals("bob.jones", response.get("username"));
        assertEquals(2026, response.get("year"));
        assertEquals(11, response.get("month"));
        assertEquals(0L, response.get("total"));
    }
}
