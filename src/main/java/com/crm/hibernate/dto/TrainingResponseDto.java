package com.crm.hibernate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingResponseDto {
    private String trainingName;
    private Date trainingDate;
    private String trainingType;
    private Number trainingDuration; // or double/long depending on your entity
    private String trainerName;
}