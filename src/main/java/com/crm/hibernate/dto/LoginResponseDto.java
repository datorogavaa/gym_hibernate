package com.crm.hibernate.dto;

public record LoginResponseDto(String token, String tokenType, long expiresIn) {
}
