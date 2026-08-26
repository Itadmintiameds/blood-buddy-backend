package com.bloodbuddy.dto.Dashboard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BloodOverviewRequest(

        @NotBlank(message = "Blood group is required")
        String bloodGroup,

        @Min(value = 0, message = "Units cannot be negative")
        Integer unitsAvailable
) {
}
