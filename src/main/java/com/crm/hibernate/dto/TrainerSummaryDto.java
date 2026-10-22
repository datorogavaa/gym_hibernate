package com.crm.hibernate.dto;

import com.crm.hibernate.entity.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerSummaryDto {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType specialization;
}