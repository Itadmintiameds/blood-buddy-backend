package com.bloodbuddy.services.Superadmin;

import com.bloodbuddy.dto.Superadmin.SuperAdminLoginRequest;
import com.bloodbuddy.dto.Superadmin.SuperAdminLoginResponse;
import com.bloodbuddy.dto.Superadmin.SuperAdminUserRequest;
import com.bloodbuddy.entity.Superadmin.SuperadminUser;
import com.bloodbuddy.exception.InvalidCredentialsException;
import com.bloodbuddy.exception.PasswordMismatchException;
import com.bloodbuddy.exception.ResourceAlreadyExistsException;
import com.bloodbuddy.jwt.JwtService;
import com.bloodbuddy.repository.superadmin.SuperadminLoginRepository;
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

    public String register(SuperAdminLoginRequest request) {

        if (!request.password()
                .equals(request.retypePassword())) {

            throw new PasswordMismatchException(
                    "Password and retype password do not match"
            );
        }

        if (superadminLoginRepository.existsByEmail(request.email())) {

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


    public SuperAdminLoginResponse login(@Valid SuperAdminUserRequest request) {

        SuperadminUser superAdminuser = superadminLoginRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

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

        String accessToken = jwtService.generateToken(
                superAdminuser.getId(),
                superAdminuser.getName(),
                superAdminuser.getEmail(),
                superAdminuser.getRole().name()
        );

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