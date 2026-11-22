package com.crm.hibernate.integration.service;

import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadNotifierTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    void sendsTrainingUpdateToActiveMq() throws JMSException {
        WorkloadNotifier notifier = new WorkloadNotifier(jmsTemplate);
        LocalDate date = LocalDate.of(2026, 11, 19);

        notifier.notify("bob.jones", "Bob", "Jones", true, date, 12.5,
                TrainerWorkloadRequest.ActionType.ADD);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);

        verify(jmsTemplate).convertAndSend(
                eq(WorkloadNotifier.WORKLOAD_QUEUE),
                requestCaptor.capture(),
                postProcessorCaptor.capture()
        );

        TrainerWorkloadRequest sentRequest = requestCaptor.getValue();
        assertEquals("bob.jones", sentRequest.trainerUsername());
        assertEquals(12, sentRequest.trainingDuration());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, sentRequest.actionType());

        Message mockMessage = mock(Message.class);
        postProcessorCaptor.getValue().postProcessMessage(mockMessage);

        ArgumentCaptor<String> txIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMessage).setStringProperty(eq(WorkloadNotifier.TRANSACTION_ID_KEY), txIdCaptor.capture());
        assertNotNull(UUID.fromString(txIdCaptor.getValue()));
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
                    eq(WorkloadNotifier.WORKLOAD_QUEUE),
                    any(TrainerWorkloadRequest.class),
                    any(MessagePostProcessor.class)
            );
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}