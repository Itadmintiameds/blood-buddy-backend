package com.bloodbuddy.services.Superadmin;

import com.bloodbuddy.dto.Superadmin.SuperadminResponse;
import com.bloodbuddy.dto.Superadmin.SuperadminUpdateRequest;
import com.bloodbuddy.entity.Superadmin.Superadmin;
import com.bloodbuddy.repository.superadmin.SuperadminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperadminService {


    private final SuperadminRepository superadminRepository;

    public List<SuperadminResponse> getAllSuperadmins() {

        return superadminRepository.findAll()
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

        Superadmin superadmin =superadminRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Superadmin record not found"
                                ));

        superadmin.setBloodCentreName(
                request.bloodCentreName().trim()
        );

        superadmin.setCategory(
                request.category().trim()
        );

        superadmin.setMobileNumber(
                request.mobileNumber()
        );

        superadmin.setAddress(
                request.address().trim()
        );

        Superadmin updated = superadminRepository.save(superadmin);

        return new SuperadminResponse(
                updated.getBloodCentre().getId(),
                updated.getBloodCentreName(),
                updated.getCategory(),
                updated.getMobileNumber(),
                updated.getAddress()

        );
    }
}