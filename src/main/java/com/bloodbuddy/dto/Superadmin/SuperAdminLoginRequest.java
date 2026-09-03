package com.bloodbuddy.dto.Superadmin;

import com.bloodbuddy.entity.Superadmin.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SuperAdminLoginRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Phone number must contain 10 digits"
        )
        String phoneNumber,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Retype password is required")
        String retypePassword,

        Role role

) {
}