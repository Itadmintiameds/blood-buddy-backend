package com.bloodbuddy.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BloodAvailabilityResponse {

    private Long id;
    private String bloodGroup;
    private Integer unitsAvailable;
}