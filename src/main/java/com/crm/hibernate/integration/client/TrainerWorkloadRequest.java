package com.crm.hibernate.integration.client;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        LocalDate trainingDate,
        Double trainingDuration,
        ActionType actionType) {

    public enum ActionType { ADD, DELETE }
}
