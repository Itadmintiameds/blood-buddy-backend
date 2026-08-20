package com.bloodbuddy.services;

import com.bloodbuddy.dto.LoginRequest;
import com.bloodbuddy.dto.LoginResponse;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.repository.BloodCentreRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final BloodCentreRepository bloodCentreRepository;

    public LoginService(BloodCentreRepository bloodCentreRepository) {
        this.bloodCentreRepository = bloodCentreRepository;
    }

    public LoginResponse login(LoginRequest request) {

        // 1. Check email
        BloodCentreReg bloodCentre = bloodCentreRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        // 2. Check password
        boolean matches = request.password()
                .equals(bloodCentre.getPassword());

        if (!matches) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Login successful
        return new LoginResponse(
                "Login successful",
                bloodCentre.getEmail()
        );
    }

    public LoginResponse logins(LoginRequest request) {

        BloodCentreReg bloodCentre = bloodCentreRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password1"));

        boolean matches = request.password()
                .equals(bloodCentre.getPassword());

        if (!matches) {
            throw new RuntimeException("Invalid email or password2");
        }

        return new LoginResponse(
                "Login successful",
                bloodCentre.getEmail()
        );
    }
}