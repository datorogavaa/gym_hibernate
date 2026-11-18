package com.crm.hibernate.integration.service;

import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import com.crm.hibernate.integration.client.WorkloadClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadNotifierTest {

    @Mock
    private WorkloadClient workloadClient;

    @Test
    void notifiesWorkloadClientWithTrainingUpdate() {
        WorkloadNotifier notifier = new WorkloadNotifier(workloadClient);
        LocalDate date = LocalDate.of(2026, 11, 19);

        notifier.notify("bob.jones", "Bob", "Jones", true, date, 12.5,
                TrainerWorkloadRequest.ActionType.ADD);

        ArgumentCaptor<TrainerWorkloadRequest> request =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadClient).updateWorkload(request.capture());
        assertEquals("bob.jones", request.getValue().trainerUsername());
        assertEquals(12.5, request.getValue().trainingDuration());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, request.getValue().actionType());
    }

    @Test
    void getsWorkloadFromClient() {
        WorkloadNotifier notifier = new WorkloadNotifier(workloadClient);
        Map<String, Object> workload = Map.of("trainerUsername", "bob.jones");
        when(workloadClient.getWorkload("bob.jones")).thenReturn(workload);

        assertEquals(workload, notifier.getWorkload("bob.jones"));
    }

    @Test
    void getsMonthlyHoursFromClient() {
        WorkloadNotifier notifier = new WorkloadNotifier(workloadClient);
        Map<String, Object> monthly = Map.of("total", 12L);
        when(workloadClient.getMonthlyHours("bob.jones", 2026, 11)).thenReturn(monthly);

        assertEquals(monthly, notifier.getMonthlyHours("bob.jones", 2026, 11));
    }
}
