package com.crm.hibernate.integration.service;

import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadNotifierTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    void sendsTrainingUpdateToActiveMq() {
        WorkloadNotifier notifier = new WorkloadNotifier(jmsTemplate);
        LocalDate date = LocalDate.of(2026, 11, 19);

        notifier.notify("bob.jones", "Bob", "Jones", true, date, 12.5,
                TrainerWorkloadRequest.ActionType.ADD);

        ArgumentCaptor<TrainerWorkloadRequest> request =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq(WorkloadNotifier.WORKLOAD_QUEUE), request.capture());
        assertEquals("bob.jones", request.getValue().trainerUsername());
        assertEquals(12, request.getValue().trainingDuration());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, request.getValue().actionType());
    }

    @Test
    void defersTrainingUpdateUntilTransactionCommit() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            WorkloadNotifier notifier = new WorkloadNotifier(jmsTemplate);

            notifier.notify("bob.jones", "Bob", "Jones", true,
                    LocalDate.of(2026, 11, 19), 12.5,
                    TrainerWorkloadRequest.ActionType.ADD);

            verifyNoInteractions(jmsTemplate);

            for (TransactionSynchronization synchronization :
                    TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(jmsTemplate).convertAndSend(
                    org.mockito.ArgumentMatchers.eq(WorkloadNotifier.WORKLOAD_QUEUE),
                    org.mockito.ArgumentMatchers.any(TrainerWorkloadRequest.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}
