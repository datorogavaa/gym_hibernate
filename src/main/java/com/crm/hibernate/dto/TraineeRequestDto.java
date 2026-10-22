package com.crm.hibernate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private Date dateOfBirth;
    private String address;
    private Boolean isActive;

}
