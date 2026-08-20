package com.bloodbuddy.services;

import com.bloodbuddy.dto.ResetPasswordRequest;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.exceptionhandling.PasswordResetException;
import com.bloodbuddy.repository.BloodCentreRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BloodCentreRepository bloodCentreRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService(
            BloodCentreRepository bloodCentreRepository,
            BCryptPasswordEncoder passwordEncoder) {

        this.bloodCentreRepository = bloodCentreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String resetPassword(ResetPasswordRequest request) {

//        if (!request.newPassword().equals(request.confirmPassword())) {
//            throw new RuntimeException("Passwords do not match");
//        }

        if (request == null) {
            throw new PasswordResetException("Request body is required");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new PasswordResetException("Email is required");
        }

        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new PasswordResetException("New password is required");
        }

        if (request.confirmPassword() == null || request.confirmPassword().isBlank()) {
            throw new PasswordResetException("Confirm password is required");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordResetException("Passwords do not match");
        }

        BloodCentreReg bloodCentre =
                bloodCentreRepository.findByEmail(request.email())
                        .orElseThrow(() ->
                                new RuntimeException("Email not found"));

        bloodCentre.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        // Don't store confirmPassword
        bloodCentre.setConfirmPassword(null);

        bloodCentreRepository.save(bloodCentre);

        return "Password reset successfully";
    }
}