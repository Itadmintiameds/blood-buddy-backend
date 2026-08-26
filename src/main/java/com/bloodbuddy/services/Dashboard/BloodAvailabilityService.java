package com.bloodbuddy.services.Dashboard;

import com.bloodbuddy.dto.Dashboard.BloodAvailabilityRequest;
import com.bloodbuddy.dto.Dashboard.BloodOverviewRequest;
import com.bloodbuddy.dto.Dashboard.BloodOverviewResponse;
import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import com.bloodbuddy.repository.Dashboard.BloodAvailabilityRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodAvailabilityService {

    private final BloodAvailabilityRepository repository;

    public BloodAvailability addAvailability(
            @Valid BloodAvailabilityRequest request) {

//        if (repository.existsByBloodGroup(
//                request.bloodGroup().toUpperCase())) {
//
//            throw new RuntimeException(
//                    "Blood group already exists: " + request.bloodGroup()
//            );
//        }

        BloodAvailability availability = BloodAvailability.builder()
                .bloodGroup(request.bloodGroup().toUpperCase())
                .bloodType(request.bloodType().toUpperCase())
                .unitsAvailable(request.unitsAvailable())
                .build();

        return repository.save(availability);
    }

    public BloodAvailability addOverview(BloodOverviewRequest request) {

        BloodAvailability overview = BloodAvailability.builder()
                .bloodGroup(request.bloodGroup().toUpperCase())
                .unitsAvailable(request.unitsAvailable())
                .build();

        return repository.save(overview);
    }

    // Update availability
    public BloodAvailability updateAvailability(
            String bloodGroup,
            BloodAvailabilityRequest request) {

        BloodAvailability availability =
                repository.findByBloodGroup(bloodGroup.toUpperCase())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Blood group not found: " + bloodGroup
                                ));

        availability.setUnitsAvailable(request.unitsAvailable());

        return repository.save(availability);
    }

    // Get all blood availability
    public List<BloodOverviewResponse> getAllAvailability() {

        return repository.findAll()
                .stream()
                .map(item -> new BloodOverviewResponse(
                        item.getBloodGroup(),
                        item.getUnitsAvailable()
                ))
                .toList();
    }
}