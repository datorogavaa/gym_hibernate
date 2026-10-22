package com.crm.hibernate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequestDto {

    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    @Size(max = 100, message = "Training name must not exceed 100 characters")
    private String trainingName;

    @NotNull(message = "Training date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be greater than 0")
    private Double trainingDuration;

    @NotBlank(message = "Authentication username is required")
    private String authUser;

    @NotBlank(message = "Authentication password is required")
    private String authPass;
}