package com.bloodbuddy.services.Dashboard;

import com.bloodbuddy.dto.Dashboard.BloodAvailabilityRequest;
import com.bloodbuddy.dto.Dashboard.BloodAvailabilityResponse;
import com.bloodbuddy.dto.Dashboard.BloodOverviewRequest;
import com.bloodbuddy.dto.Dashboard.BloodOverviewResponse;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import com.bloodbuddy.exception.ResourceBloodGroupAndBloodCentre;
import com.bloodbuddy.exception.ResourceNotFoundException;
import com.bloodbuddy.repository.BloodCentreRepository;
import com.bloodbuddy.repository.Dashboard.BloodAvailabilityRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BloodAvailabilityService {

    private final BloodAvailabilityRepository bloodAvailabilityRepository;
    private final BloodCentreRepository bloodCentreRepository;

    public BloodAvailability addAvailability(@Valid BloodAvailabilityRequest request) {

        BloodCentreReg bloodCentre = bloodCentreRepository.findById(request.bloodCentreId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Blood centre not found with id: "
                                                + request.bloodCentreId()
                                ));

        Optional<BloodAvailability> existing = bloodAvailabilityRepository.findByBloodCentreIdAndBloodGroupAndBloodType(
                                request.bloodCentreId(),
                                request.bloodGroup().toUpperCase(),
                                request.bloodType().toUpperCase()
                        );



        if (existing.isPresent()) {
            throw new ResourceBloodGroupAndBloodCentre(
                    "Blood availability already exists for this blood centre"
            );
        }

        BloodAvailability availability = BloodAvailability.builder()
                .bloodGroup(request.bloodGroup().toUpperCase())
                .bloodType(request.bloodType().toUpperCase())
                .unitsAvailable(request.unitsAvailable())
                .bloodCentre(bloodCentre)
                .build();

        return bloodAvailabilityRepository.save(availability);
    }

    public BloodAvailability addOverview(BloodOverviewRequest request) {

        BloodAvailability overview = BloodAvailability.builder()
                .bloodGroup(request.bloodGroup().toUpperCase())
                .unitsAvailable(request.unitsAvailable())
                .build();

        return bloodAvailabilityRepository.save(overview);
    }

    public List<BloodAvailabilityResponse> getBloodAvailability() {

        return bloodAvailabilityRepository.findAll()
                .stream()
                .map(item -> new BloodAvailabilityResponse(
                        item.getBloodCentre().getId(),
                        item.getBloodGroup(),
                        item.getBloodType(),
                        item.getUnitsAvailable()
                ))
                .toList();
    }

//    // Update availability
//    public BloodAvailability updateAvailability(
//            String bloodGroup,
//            BloodAvailabilityRequest request) {
//
//        BloodAvailability availability =
//                bloodAvailabilityRepository.findByBloodGroup(bloodGroup.toUpperCase())
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Blood group not found: " + bloodGroup
//                                ));
//
//        availability.setUnitsAvailable(request.unitsAvailable());
//
//        return bloodAvailabilityRepository.save(availability);
//    }

    // Get all blood availability
    public List<BloodOverviewResponse> getAllAvailability() {

        return bloodAvailabilityRepository.findAll()
                .stream()
                .map(item -> new BloodOverviewResponse(
                        item.getBloodGroup(),
                        item.getUnitsAvailable()
                ))
                .toList();
    }
}