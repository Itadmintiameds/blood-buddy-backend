package com.bloodbuddy.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(

        @NotBlank(message = "Mobile number is required")
        String mobileNumber

) {
}