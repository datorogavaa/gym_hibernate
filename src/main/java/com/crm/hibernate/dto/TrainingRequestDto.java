package com.crm.hibernate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequestDto {
    @NotBlank private String traineeUsername;
    @NotBlank private String trainerUsername;
    @NotBlank private String trainingName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    @NotNull private Date trainingDate;
    @NotNull private Double trainingDuration;
    @NotBlank private String authUser;
    @NotBlank private String authPass;
}
