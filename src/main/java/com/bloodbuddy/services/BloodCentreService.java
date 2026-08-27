package com.bloodbuddy.services;

import com.bloodbuddy.dto.BloodCentreRegistrationRequest;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.repository.BloodCentreRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BloodCentreService {

    private final BloodCentreRepository bloodCentreRepository;
    private final PasswordEncoder passwordEncoder;

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


        String bloodEmail = request.email()
                .trim()
                .toLowerCase();

        boolean licenseExists =
                bloodCentreRepository.existsByLicenseNumber(licenseNumber);

        boolean emailExists =
                bloodCentreRepository.existsByEmail(bloodEmail);

        // 5. Both exist
        if (licenseExists && emailExists) {
            throw new RuntimeException(
                    "Blood Centre License Number already exists, Email already exists"
            );
        }

        // 6. Only license exists
        if (licenseExists) {
            throw new RuntimeException(
                    "Blood Centre License Number already exists"
            );
        }
        // 7. Only email exists
        if (emailExists) {
            throw new RuntimeException(
                    "Email already exists"
            );
        }


        BloodCentreReg bloodCentre = new BloodCentreReg();

        bloodCentre.setBloodCentreName(
                request.bloodCentreName().trim()
        );

        bloodCentre.setLicenseNumber(
                licenseNumber
        );
        bloodCentre.setCategory(
                request.category().trim()
        );

        bloodCentre.setDateOfExpiry(
                request.dateOfExpiry()
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
        bloodCentre.setDistrict(
                request.district().trim()
        );

        bloodCentre.setCity(
                request.city().trim()
        );
        bloodCentre.setPincode(
                request.pincode()
        );

        return bloodCentreRepository.save(bloodCentre);
    }
}