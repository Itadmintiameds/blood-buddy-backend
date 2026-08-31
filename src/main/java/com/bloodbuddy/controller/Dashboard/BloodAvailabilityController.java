package com.bloodbuddy.controller.Dashboard;

import com.bloodbuddy.dto.Dashboard.BloodAvailabilityRequest;
import com.bloodbuddy.dto.Dashboard.BloodAvailabilityResponse;
import com.bloodbuddy.dto.Dashboard.BloodOverviewRequest;
import com.bloodbuddy.dto.Dashboard.BloodOverviewResponse;
import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import com.bloodbuddy.services.Dashboard.BloodAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class BloodAvailabilityController {

    private final BloodAvailabilityService service;

    @GetMapping("/bloodavail_overview")
    public ResponseEntity<List<BloodOverviewResponse>> getDashboard() {
        return ResponseEntity.ok(service.getAllAvailability());
    }

    @GetMapping("/bloodgroup_availability")
    public ResponseEntity<List<BloodAvailabilityResponse>> getBloodAvailability() {
        return ResponseEntity.ok(service.getBloodAvailability());
    }

    // Add new blood availability
    @PostMapping("/add_overview")
    public ResponseEntity<BloodAvailability> addOverview(
            @Valid @RequestBody BloodOverviewRequest bloodOverviewRequest) {

        return ResponseEntity.ok(
                service.addOverview(bloodOverviewRequest)
        );
    }

    // Add new blood availability
    @PostMapping("/add_availability")
    public ResponseEntity<BloodAvailability> addAvailability(
            @Valid @RequestBody BloodAvailabilityRequest request) {

        return ResponseEntity.ok(
                service.addAvailability(request)
        );
    }


    // Update units
    @PutMapping("/{bloodGroup}")
    public ResponseEntity<BloodAvailability> updateAvailability(
            @PathVariable String bloodGroup,
            @Valid @RequestBody BloodAvailabilityRequest request) {

        BloodAvailability response =
                service.updateAvailability(bloodGroup, request);

        return ResponseEntity.ok(response);
    }


}