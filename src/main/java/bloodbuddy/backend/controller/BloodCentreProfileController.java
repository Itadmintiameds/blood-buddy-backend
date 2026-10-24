package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.centre.BloodCentreProfileUpdateRequest;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.BloodCentreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Blood-centre staff view and edit their own centre's profile; the centre comes from the token.
 *  Superadmin edits any centre via PUT /admin/blood-centres/{id}. */
@RestController
@RequestMapping("/blood-centre/profile")
@PreAuthorize("hasRole('BLOOD_CENTRE')")
@RequiredArgsConstructor
public class BloodCentreProfileController {

    private final BloodCentreService bloodCentreService;

    @GetMapping
    public ResponseEntity<ApiResponse<BloodCentreResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                bloodCentreService.getCentre(ownCentre(principal))));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<BloodCentreResponse>> updateProfile(
            @Valid @RequestBody BloodCentreProfileUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                bloodCentreService.updateProfile(ownCentre(principal), request, principal.getUsername())));
    }

    // Staff may only touch their own centre; the centre id comes from the token, never the client.
    private Long ownCentre(CustomUserDetails principal) {
        if (principal.getBloodCentreId() == null) {
            throw new BadRequestException("This account is not linked to a blood centre");
        }
        return principal.getBloodCentreId();
    }
}
