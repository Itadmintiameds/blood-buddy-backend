package com.bloodbuddy.services;

import com.bloodbuddy.dto.LoginRequest;
import com.bloodbuddy.dto.LoginResponse;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.repository.BloodCentreRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginService {

    private final BloodCentreRepository bloodCentreRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        // 1. Check email
        BloodCentreReg bloodCentre = bloodCentreRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        // 2. Check password using BCrypt
        boolean matches = passwordEncoder.matches(
                request.password(),
                bloodCentre.getPassword()
        );

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