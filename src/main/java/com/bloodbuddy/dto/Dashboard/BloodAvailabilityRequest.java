package com.bloodbuddy.dto.Dashboard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BloodAvailabilityRequest(

        @NotBlank(message = "Blood type is required")
        String bloodType,

        @NotBlank(message = "Blood group is required")
        @Pattern(
                regexp = "^(A|B|AB|O)[+-]$",
                message = "Invalid blood group. Use A+, A-, B+, B-, AB+, AB-, O+, or O-"
        )
        String bloodGroup,

        @NotNull(message = "Units availability is required")
        @Min(
                value = 0,
                message = "Units availability cannot be negative"
        )
        Integer unitsAvailable,

        @NotNull(message = "Blood centre ID is required")
        Long bloodCentreId
) {
}