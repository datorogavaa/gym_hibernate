package com.crm.hibernate.integration.service;

import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.UUID;

/** Single entry point used by the main service to push workload changes. */
@Service
public class WorkloadNotifier {

    private static final Logger log = LoggerFactory.getLogger(WorkloadNotifier.class);
    public static final String WORKLOAD_QUEUE = "gym.workload.updates";
    public static final String TRANSACTION_ID_KEY = "transactionId";

    private final JmsTemplate jmsTemplate;

    public WorkloadNotifier(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void notify(String username, String firstName, String lastName, Boolean active,
                       LocalDate date, Double duration, TrainerWorkloadRequest.ActionType action) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                username, firstName, lastName, active, date,
                duration == null ? null : duration.intValue(), action);

        // Capture MDC transactionId before transaction commits / filters clear MDC
        String existingTxId = MDC.get(TRANSACTION_ID_KEY);
        String txId = (existingTxId != null && !existingTxId.isBlank())
                ? existingTxId
                : UUID.randomUUID().toString();

        Runnable send = () -> {
            log.info("[txId={}] [operation] enqueueing workload update after transaction commit {}", txId, request);
            jmsTemplate.convertAndSend(WORKLOAD_QUEUE, request, message -> {
                message.setStringProperty(TRANSACTION_ID_KEY, txId);
                return message;
            });
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            send.run();
                        }
                    });
            return;
        }

        send.run();
    }
}