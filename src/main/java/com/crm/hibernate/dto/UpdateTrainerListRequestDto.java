package com.crm.hibernate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainerListRequestDto {
    private Set<String> trainerUsernames; // Required list of trainer usernames
}