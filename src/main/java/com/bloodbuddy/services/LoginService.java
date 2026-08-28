package com.bloodbuddy.services;

import com.bloodbuddy.dto.LoginRequest;
import com.bloodbuddy.dto.LoginResponse;
import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.exception.ResourceAlreadyExistsException;
import com.bloodbuddy.repository.BloodCentreRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginService {

    private final BloodCentreRepository bloodCentreRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(
            LoginRequest request,
            HttpSession session) {

        String email = request.email()
                .trim()
                .toLowerCase();

        BloodCentreReg bloodCentre =
                bloodCentreRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceAlreadyExistsException(
                                        "Invalid email or password"
                                )
                        );

        boolean matches = passwordEncoder.matches(
                request.password(),
                bloodCentre.getPassword()
        );

        if (!matches) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        String loggedInEmail =
                (String) session.getAttribute("USER_EMAIL");

        if (loggedInEmail != null) {

            return new LoginResponse(
                    "Already logged in",
                    loggedInEmail
            );
        }

        session.setAttribute(
                "USER_ID",
                bloodCentre.getId()
        );

        session.setAttribute(
                "USER_EMAIL",
                bloodCentre.getEmail()
        );

        return new LoginResponse(
                "Login successful",
                bloodCentre.getEmail()
        );
    }
}