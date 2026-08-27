package com.bloodbuddy.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record BloodCentreRegistrationRequest(

        @NotBlank(message = "Blood Centre Name is required")
        String bloodCentreName,

        @NotBlank(message = "License Number is required")
        @Size(
                min = 5,
                max = 30,
                message = "License Number must be between 5 and 30 characters"
        )
        @Pattern(
                regexp = "^[A-Za-z0-9/-]+$",
                message = "License Number can contain only letters, numbers, / and -"
        )
        String licenseNumber,

        @NotBlank(message = "Category is required")
        @Pattern(
                regexp = "^(Government|Private|Charitable|Redcross)$",
                message = "Category must be Government, Private, Charitable, or Redcross"
        )
        String category,

        @NotNull(message = "Date of Expiry is required")
        LocalDate dateOfExpiry,


        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

                @NotBlank(message = "Mobile Number is required")
        @Pattern(
                regexp = "^[6-9][0-9]{9}$",
                message = "Invalid mobile number"
        )

        String mobileNumber,


        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password,

        @NotBlank(message = "Confirm Password is required")
        String confirmPassword,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "City is required")
        String city,


        @NotBlank(message = "Pincode is required")
        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "Pincode must contain exactly 6 digits"
        )
        String pincode
) {
}