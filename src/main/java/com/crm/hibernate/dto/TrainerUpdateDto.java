package com.crm.hibernate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerUpdateDto {
    private String firstName;
    private String lastName;
    private Boolean isActive;
}