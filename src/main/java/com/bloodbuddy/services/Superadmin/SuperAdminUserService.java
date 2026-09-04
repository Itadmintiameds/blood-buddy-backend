package com.bloodbuddy.services.Superadmin;

import com.bloodbuddy.dto.Superadmin.SuperAdminLoginRequest;
import com.bloodbuddy.dto.Superadmin.SuperAdminLoginResponse;
import com.bloodbuddy.dto.Superadmin.SuperAdminUserRequest;
import com.bloodbuddy.entity.Superadmin.SuperadminUser;
import com.bloodbuddy.exception.AlreadyLoggedInException;
import com.bloodbuddy.exception.InvalidCredentialsException;
import com.bloodbuddy.exception.PasswordMismatchException;
import com.bloodbuddy.exception.ResourceAlreadyExistsException;
import com.bloodbuddy.jwt.JwtService;
import com.bloodbuddy.repository.superadmin.SuperadminLoginRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminUserService {

    private final SuperadminLoginRepository superadminLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(
            SuperAdminLoginRequest request,
            HttpSession session) {

        if (!request.password()
                .equals(request.retypePassword())) {

            throw new PasswordMismatchException(
                    "Password and retype password do not match"
            );
        }

        if (superadminLoginRepository
                .existsByEmail(request.email())) {

            throw new ResourceAlreadyExistsException(
                    "Email already registered"
            );
        }

        if (superadminLoginRepository
                .existsByPhoneNumber(request.phoneNumber())) {

            throw new ResourceAlreadyExistsException(
                    "Phone number already registered"
            );
        }

        SuperadminUser superAdmin = new SuperadminUser();

        superAdmin.setName(request.name());
        superAdmin.setEmail(request.email());
        superAdmin.setPhoneNumber(request.phoneNumber());

        superAdmin.setPassword(
                passwordEncoder.encode(request.password())
        );

        superAdmin.setRole(request.role());

        superadminLoginRepository.save(superAdmin);

        return "Superadmin registered successfully";
    }

    public SuperAdminLoginResponse login(
            @Valid SuperAdminUserRequest request,
            HttpSession session) {

        // 1. Get email
        String email = request.email()
                .trim()
                .toLowerCase();

        // 2. Find Superadmin
        SuperadminUser superAdminuser =
                superadminLoginRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid email or password"
                                )
                        );

        // 3. Check password
        boolean passwordMatches =
                passwordEncoder.matches(
                        request.password(),
                        superAdminuser.getPassword()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        // 4. Check if SAME Superadmin is already logged in
        String loggedInEmail =
                (String) session.getAttribute(
                        "SUPERADMIN_EMAIL"
                );

        if (loggedInEmail != null &&
                loggedInEmail.equalsIgnoreCase(email)) {

            throw new AlreadyLoggedInException(
                    "Superadmin is already logged in"
            );
        }

        // 5. Generate JWT
        String accessToken =
                jwtService.generateToken(
                        superAdminuser.getId(),
                        superAdminuser.getName(),
                        superAdminuser.getEmail(),
                        superAdminuser.getRole().name()
                );

        // 6. Store current Superadmin in session
        session.setAttribute(
                "SUPERADMIN_ID",
                superAdminuser.getId()
        );

        session.setAttribute(
                "SUPERADMIN_EMAIL",
                superAdminuser.getEmail()
        );

        // 7. Return successful login response
        return new SuperAdminLoginResponse(
                "Login successful",
                superAdminuser.getId(),
                superAdminuser.getName(),
                superAdminuser.getEmail(),
                superAdminuser.getRole().name(),
                accessToken
        );
    }
}