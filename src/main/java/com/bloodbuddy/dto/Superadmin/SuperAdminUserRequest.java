package com.bloodbuddy.dto.Superadmin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SuperAdminUserRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password




        ) {
}
