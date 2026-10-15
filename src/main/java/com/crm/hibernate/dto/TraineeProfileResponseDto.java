package com.crm.hibernate.dto;

import com.crm.hibernate.entity.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeProfileResponseDto {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String address;
    private boolean isActive;
    private List<TrainerDto> trainers;

    // Nested DTO for the Trainer list
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrainerDto {
        private String username;
        private String firstName;
        private String lastName;
        private TrainingType specialization;
    }
}