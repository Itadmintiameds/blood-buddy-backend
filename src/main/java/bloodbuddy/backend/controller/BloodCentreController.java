package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.centre.BloodCentreFilterRequest;
import bloodbuddy.backend.dto.centre.BloodCentreProfileUpdateRequest;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.centre.BloodCentreStatsResponse;
import bloodbuddy.backend.dto.centre.BloodCentreUpdateRequest;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.BloodCentreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Blood centre management; access is decided per role by @PreAuthorize. SUPERADMIN works on
 * any centre, BLOOD_CENTRE staff only on their own via /me (centre id taken from the token).
 * Stock lives under /blood-centres/{id}/inventory (see InventoryController).
 */
@RestController
@RequestMapping("/blood-centres")
@PreAuthorize("hasRole('SUPERADMIN')")
public class BloodCentreController {

    private final BloodCentreService bloodCentreService;

    public BloodCentreController(BloodCentreService bloodCentreService) {
        this.bloodCentreService = bloodCentreService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BloodCentreResponse>>> listCentres() {
        return ResponseEntity.ok(ApiResponse.success("Blood centres fetched", bloodCentreService.listAll()));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<BloodCentreResponse>>> listCentresPaginated(
            @ModelAttribute BloodCentreFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Blood centres fetched",
                bloodCentreService.list(filter, pageable)));
    }

    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<LocationOptionsResponse>> locationOptions() {
        return ResponseEntity.ok(ApiResponse.success("Location options fetched",
                bloodCentreService.getLocationOptions()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<BloodCentreStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success("Stats fetched", bloodCentreService.getStats()));
    }

    /** Edit a centre's profile and licence details. */
    @PutMapping("/{bloodCentreId}")
    public ResponseEntity<ApiResponse<BloodCentreResponse>> updateCentre(
            @PathVariable Long bloodCentreId,
            @Valid @RequestBody BloodCentreUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Blood centre updated",
                bloodCentreService.updateCentre(bloodCentreId, request, principal.getUsername())));
    }

    /** Blood-centre staff view their own centre's profile. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('BLOOD_CENTRE')")
    public ResponseEntity<ApiResponse<BloodCentreResponse>> getOwnProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                bloodCentreService.getCentre(ownCentre(principal))));
    }

    /** Blood-centre staff edit their own centre's profile. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('BLOOD_CENTRE')")
    public ResponseEntity<ApiResponse<BloodCentreResponse>> updateOwnProfile(
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
