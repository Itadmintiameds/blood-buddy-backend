package com.bloodbuddy.dto.Dashboard;

public record BloodAvailabilityResponse(


        Long id,
        String bloodType,
        String bloodGroup,
        Integer unitsAvailable
) {
}