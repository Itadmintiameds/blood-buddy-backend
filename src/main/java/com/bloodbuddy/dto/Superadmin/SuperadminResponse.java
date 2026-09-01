package com.bloodbuddy.dto.Superadmin;

public record SuperadminResponse(
        Long id,
        String bloodCentreName,
        String category,
        String mobileNumber,
        String address
) {
}