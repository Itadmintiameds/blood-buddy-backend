package com.bloodbuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record ResetPasswordRequest(

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
                message = "Password must contain at least 8 characters, one letter, one number and one special character"
        )
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}