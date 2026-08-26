package com.bloodbuddy.services;

import com.bloodbuddy.dto.BloodCentreRegistrationRequest;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.repository.BloodCentreRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BloodCentreService {

    private final BloodCentreRepository bloodCentreRepository;
    private final PasswordEncoder passwordEncoder;

    public BloodCentreService(
            BloodCentreRepository bloodCentreRepository,
            PasswordEncoder passwordEncoder) {

        this.bloodCentreRepository = bloodCentreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public BloodCentreReg register(
            BloodCentreRegistrationRequest request) {

        if (!request.password()
                .equals(request.confirmPassword())) {

            throw new RuntimeException(
                    "Password and Confirm Password do not match"
            );
        }

        String licenseNumber =
                request.licenseNumber()
                        .trim()
                        .toUpperCase();

        if (bloodCentreRepository
                .existsByLicenseNumber(licenseNumber)) {

            throw new RuntimeException(
                    "Blood Centre License Number already exists"
            );
        }

        BloodCentreReg bloodCentre = new BloodCentreReg();

        bloodCentre.setBloodCentreName(
                request.bloodCentreName().trim()
        );

        bloodCentre.setLicenseNumber(
                licenseNumber
        );

        bloodCentre.setMobileNumber(
                request.mobileNumber()
        );

        bloodCentre.setEmail(
                request.email()
                        .trim()
                        .toLowerCase()
        );

        bloodCentre.setPassword(
                passwordEncoder.encode(request.password())
        );

        bloodCentre.setAddress(
                request.address().trim()
        );

        bloodCentre.setPincode(
                request.pincode()
        );

        return bloodCentreRepository.save(bloodCentre);
    }
}