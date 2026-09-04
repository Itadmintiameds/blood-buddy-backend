package com.bloodbuddy.dto.Superadmin;

public record SuperAdminLoginResponse(

        String loginSuccessful,
        Long id,
        String name,
        String email,
        String role,
        String accessToken) {
}