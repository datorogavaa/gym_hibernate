package com.crm.hibernate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrainerListRequestDto {

    @NotNull(message = "Trainer usernames set cannot be null")
    private Set<String> trainerUsernames;
    @NotBlank(message = "Authentication username cannot be blank")
    private String authUser;
    @NotBlank(message = "Authentication password cannot be blank")
    private String authPass;
}