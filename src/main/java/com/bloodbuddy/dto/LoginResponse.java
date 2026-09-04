package com.bloodbuddy.dto;

public record LoginResponse(
        String message,
        long id,
        String email,
        String accessToken
) {
}