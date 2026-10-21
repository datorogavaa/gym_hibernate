package com.crm.hibernate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerUpdateDto {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private Boolean isActive;
}