package com.crm.hibernate.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Circuit breaker fallback for workload query operations.
 */
@Component
public class WorkloadClientFallbackFactory implements FallbackFactory<WorkloadClient> {

    private static final Logger log = LoggerFactory.getLogger(WorkloadClientFallbackFactory.class);

    @Override
    public WorkloadClient create(Throwable cause) {
        return new WorkloadClient() {

            @Override
            public Map<String, Object> getWorkload(String username) {
                log.error("[fallback] workload service unavailable, getWorkload failed for trainer={} cause={}",
                        username, cause.toString());
                return Map.of(
                        "trainerUsername", username,
                        "error", "Workload service unavailable"
                );
            }

            @Override
            public Map<String, Object> getMonthlyHours(String username, int year, int month) {
                log.error("[fallback] workload service unavailable, getMonthlyHours failed for trainer={} year={} month={} cause={}",
                        username, year, month, cause.toString());
                return Map.of(
                        "username", username,
                        "year", year,
                        "month", month,
                        "total", 0L
                );
            }
        };
    }
}