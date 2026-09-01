package com.bloodbuddy.dto.Superadmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SuperadminUpdateRequest(

      //  Long id,
        @NotBlank(message = "Blood centre name is required")
        String bloodCentreName,

        @NotBlank(message = "Category is required")
        String category,

        @NotBlank(message = "Mobile number is required")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Mobile number must be 10 digits"
        )
        String mobileNumber,

        @NotBlank(message = "Address is required")
        String address
) {

}