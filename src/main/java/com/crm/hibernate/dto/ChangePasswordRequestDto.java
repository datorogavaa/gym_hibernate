package com.crm.hibernate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank
    private String username;

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

}