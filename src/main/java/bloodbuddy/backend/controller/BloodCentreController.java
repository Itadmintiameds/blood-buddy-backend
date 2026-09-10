package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.centre.BloodCentreRegistrationRequest;
import bloodbuddy.backend.dto.centre.BloodCentreRegistrationResponse;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.BloodCentreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/blood-centres")
public class BloodCentreController {

    private final BloodCentreService bloodCentreService;

    public BloodCentreController(BloodCentreService bloodCentreService) {
        this.bloodCentreService = bloodCentreService;
    }

    /**
     * One endpoint for both paths: a SUPERADMIN (authenticated) creating a centre, or a centre
     * self-registering (unauthenticated). Public so onboarding needs no prior account; the
     * created login is always BLOOD_CENTRE. created_by is the admin's username, else "SELF".
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<BloodCentreRegistrationResponse>> register(
            @Valid @RequestBody BloodCentreRegistrationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        String actor = principal != null ? principal.getUsername() : "SELF";
        BloodCentreRegistrationResponse response = bloodCentreService.register(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blood centre registered successfully", response));
    }
}
