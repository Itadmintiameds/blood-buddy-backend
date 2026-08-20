package com.bloodbuddy.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(

        @NotBlank(message = "Mobile number is required")
        String mobileNumber,

        @NotBlank(message = "OTP is required")
        String otp

) {
}