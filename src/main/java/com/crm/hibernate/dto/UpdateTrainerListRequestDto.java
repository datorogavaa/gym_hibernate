package com.crm.hibernate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainerListRequestDto {

    @NotBlank(message = "Username is required")
    private String authUser;

    @NotBlank(message = "Password is required")
    private String authPass;

    @NotNull(message = "Trainer usernames set cannot be null")
    private Set<String> trainerUsernames;
}