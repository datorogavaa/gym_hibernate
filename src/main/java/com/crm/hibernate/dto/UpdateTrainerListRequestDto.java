package com.crm.hibernate.dto;

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
}