package com.bloodbuddy.dto.Dashboard;

import java.util.List;

public record BloodDashboardResponse(

        Integer bloodGroupsListed,

        Integer totalUnitsAvailable,

        Integer lowStockAlerts,

        List<BloodOverviewResponse> availability

) {
}