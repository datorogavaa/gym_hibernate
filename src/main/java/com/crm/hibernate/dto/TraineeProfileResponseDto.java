package com.crm.hibernate.dto;

import com.crm.hibernate.entity.TrainingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.core.util.Json;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")    private Date dateOfBirth;
    private String address;
    private boolean isActive;
    private List<TrainerDto> trainers;

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