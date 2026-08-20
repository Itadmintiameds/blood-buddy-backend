package com.bloodbuddy.dto.Dashboard;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class DashboardResponse {

    private long bloodGroupsListed;
    private int totalUnitsAvailable;
    private long lowStockAlerts;
    private List<BloodGroupResponse> availability;
}
