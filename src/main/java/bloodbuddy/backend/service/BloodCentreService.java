package bloodbuddy.backend.service;

import bloodbuddy.backend.constants.RoleNames;
import bloodbuddy.backend.dto.centre.BloodCentreRegistrationRequest;
import bloodbuddy.backend.dto.centre.BloodCentreRegistrationResponse;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.Users;
import bloodbuddy.backend.entity.masters.Roles;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.BloodCentresRepository;
import bloodbuddy.backend.repository.RolesRepository;
import bloodbuddy.backend.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodCentreService {

    private final BloodCentresRepository bloodCentresRepository;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    /**
     * Registers a blood centre and its linked login. Serves both self-registration (public)
     * and SUPERADMIN-created centres; the created account's role is always fixed to
     * BLOOD_CENTRE regardless of caller, so a self-registering centre cannot elevate itself.
     */
    @Transactional(readOnly = true)
    public List<BloodCentreResponse> listAll() {
        return bloodCentresRepository.findAll().stream()
                .map(BloodCentreResponse::fromEntity)
                .toList();
    }

    @Transactional
    public BloodCentreRegistrationResponse register(BloodCentreRegistrationRequest request, String actor) {
        // Registration is gated on a prior OTP verification of this email.
        emailVerificationService.assertEmailVerified(request.getEmail());

        // The centre's email doubles as the login username, so it must be free on both counts.
        if (usersRepository.existsByUsername(request.getEmail()) || usersRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists: " + request.getEmail());
        }
        if (bloodCentresRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A blood centre with this email already exists: " + request.getEmail());
        }

        // Role is looked up, never created; absence is an operator error.
        Roles bloodCentreRole = rolesRepository.findByRoleName(RoleNames.BLOOD_CENTRE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Required role '" + RoleNames.BLOOD_CENTRE + "' is missing from the roles table. "
                                + "Seed the master roles first."));

        LocalDateTime now = LocalDateTime.now();

        BloodCentres centre = new BloodCentres();
        centre.setBloodCentreName(request.getBloodCentreName());
        centre.setBloodBankCategory(request.getBloodBankCategory());
        centre.setBloodCentreLicenceNumber(request.getBloodCentreLicenceNumber());
        centre.setLicenceExpiryDate(request.getLicenceExpiryDate());
        centre.setMobileNumber(request.getMobileNumber());
        centre.setEmail(request.getEmail());
        centre.setAddress(request.getAddress());
        centre.setDistrict(request.getDistrict());
        centre.setCity(request.getCity());
        centre.setPincode(request.getPincode());
        centre.setLatitude(request.getLatitude());
        centre.setLongitude(request.getLongitude());
        centre.setLocationUrl(request.getLocationUrl());
        centre.setIsActive(true);
        centre.setCreatedAt(now);
        centre.setCreatedBy(actor);
        centre = bloodCentresRepository.save(centre);

        Users user = new Users();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(bloodCentreRole);
        user.setBloodCentre(centre);
        user.setIsActive(true);
        user.setCreatedAt(now);
        user.setCreatedBy(actor);
        usersRepository.save(user);

        // Consume the verification so the OTP cannot be reused for another registration.
        emailVerificationService.clearVerification(request.getEmail());

        return BloodCentreRegistrationResponse.builder()
                .bloodCentreId(centre.getBloodCentreId())
                .bloodCentreName(centre.getBloodCentreName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(bloodCentreRole.getRoleName())
                .build();
    }
}
