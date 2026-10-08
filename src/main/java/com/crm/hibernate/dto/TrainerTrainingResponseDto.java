package com.crm.hibernate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerTrainingResponseDto {
    private String trainingName;
    private Date trainingDate;
    private String trainingType;
    private Number trainingDuration; // with .intValue() handled if needed
    private String traineeName;
}