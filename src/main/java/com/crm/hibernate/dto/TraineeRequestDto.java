package com.crm.hibernate.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeRequestDto {
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private Date dateOfBirth;
    private String address;
    private Boolean isActive;

}
