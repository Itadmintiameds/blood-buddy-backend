package com.bloodbuddy.services.Superadmin;

import com.bloodbuddy.dto.Superadmin.SuperadminResponse;
import com.bloodbuddy.dto.Superadmin.SuperadminUpdateRequest;
import com.bloodbuddy.entity.Superadmin.Bloodbank;
import com.bloodbuddy.repository.superadmin.BloodbankRepository;
import com.bloodbuddy.repository.superadmin.SuperadminLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodbankService {


    private final BloodbankRepository bloodbankRepository;
    private final SuperadminLoginRepository superadminLoginRepository;

    public List<SuperadminResponse> getAllSuperadmins() {

        return bloodbankRepository.findAll()
                .stream()
                .map(superadmin -> new SuperadminResponse(
                        superadmin.getId(),
                        superadmin.getBloodCentreName(),
                        superadmin.getCategory(),
                        superadmin.getMobileNumber(),
                        superadmin.getAddress()

                ))
                .toList();
    }

    public SuperadminResponse updateSuperadmin(

            Long id, SuperadminUpdateRequest request) {

        Bloodbank bloodbank =bloodbankRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Superadmin record not found"
                                ));

        bloodbank.setBloodCentreName(
                request.bloodCentreName().trim()
        );

        bloodbank.setCategory(
                request.category().trim()
        );

        bloodbank.setMobileNumber(
                request.mobileNumber()
        );

        bloodbank.setAddress(
                request.address().trim()
        );

        Bloodbank updated = bloodbankRepository.save(bloodbank);

        return new SuperadminResponse(
                updated.getBloodCentre().getId(),
                updated.getBloodCentreName(),
                updated.getCategory(),
                updated.getMobileNumber(),
                updated.getAddress()

        );
    }
}