package com.bloodbuddy.dto.Superadmin;

public record SuperAdminLoginResponse(

        String loginSuccessful, long id,
        String email,
        String role

) {
}