package com.crm.hibernate.integration.service;

import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import com.crm.hibernate.integration.client.WorkloadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/** Single entry point used by the main service to push workload changes. */
@Service
public class WorkloadNotifier {

    private static final Logger log = LoggerFactory.getLogger(WorkloadNotifier.class);

    private final WorkloadClient client;

    public WorkloadNotifier(WorkloadClient client) {
        this.client = client;
    }

    public void notify(String username, String firstName, String lastName, Boolean active,
                       LocalDate date, Double duration, TrainerWorkloadRequest.ActionType action) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                username, firstName, lastName, active, date, duration, action);
        log.info("[operation] sending workload update {}", request);
        client.updateWorkload(request);
    }
    public Map<String, Object> getWorkload(String username) {
        log.info("[operation] fetching workload for trainer {}", username);
        return client.getWorkload(username);
    }

    public Map<String, Object> getMonthlyHours(String username, int year, int month) {
        log.info("[operation] fetching monthly workload for trainer {} ({}-{})", username, year, month);
        return client.getMonthlyHours(username, year, month);
    }
}
