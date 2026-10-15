package com.crm.hibernate.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDto {

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
}
