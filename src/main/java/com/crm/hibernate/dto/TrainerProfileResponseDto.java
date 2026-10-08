package com.crm.hibernate.dto;

import com.crm.hibernate.entity.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerProfileResponseDto {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType specialization;
    private boolean isActive;
    private List<TraineeDto> trainees;

    // Nested DTO for the Trainee list belonging to this Trainer
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TraineeDto {
        private String username;
        private String firstName;
        private String lastName;
    }
}