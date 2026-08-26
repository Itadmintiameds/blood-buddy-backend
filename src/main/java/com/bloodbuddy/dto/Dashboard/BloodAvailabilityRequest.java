package com.bloodbuddy.dto.Dashboard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BloodAvailabilityRequest(

        @NotBlank(message = "Blood group is required")
        String bloodGroup,
        @NotBlank(message = "Blood Type is required")
        String bloodType,

        @Min(value = 0, message = "Units cannot be negative")
        Integer unitsAvailable
) {
}