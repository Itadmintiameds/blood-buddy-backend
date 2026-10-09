package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.dto.donor.DonorDeactivateRequest;
import bloodbuddy.backend.dto.donor.DonorFilterRequest;
import bloodbuddy.backend.dto.donor.DonorLockRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.donor.DonorStatsResponse;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Superadmin views over all registered donors (secured by /admin/** -> SUPERADMIN). */
@RestController
@RequestMapping("/admin/donors")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminDonorController {

    private final DonorService donorService;

    public AdminDonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DonorResponse>>> listDonors() {
        return ResponseEntity.ok(ApiResponse.success("Donors fetched", donorService.listAll()));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<DonorResponse>>> listDonorsPaginated(
            @ModelAttribute DonorFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Donors fetched", donorService.list(filter, pageable)));
    }

    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<LocationOptionsResponse>> locationOptions() {
        return ResponseEntity.ok(ApiResponse.success("Location options fetched",
                donorService.getLocationOptions()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DonorStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.success("Stats fetched", donorService.getStats()));
    }

    /** Temporarily lock a donor for a date range (illness / out-of-station); auto-expires. */
    @PatchMapping("/{donorId}/lock")
    public ResponseEntity<ApiResponse<DonorResponse>> lockDonor(
            @PathVariable Long donorId,
            @Valid @RequestBody DonorLockRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Donor locked",
                donorService.lockDonor(donorId, request, principal.getUsername())));
    }

    /** Permanently deactivate a donor (medical grounds / deceased / relocated). */
    @PatchMapping("/{donorId}/deactivate")
    public ResponseEntity<ApiResponse<DonorResponse>> deactivateDonor(
            @PathVariable Long donorId,
            @Valid @RequestBody DonorDeactivateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Donor deactivated",
                donorService.deactivateDonor(donorId, request, principal.getUsername())));
    }

    /** Restore a donor to active, clearing any lock / deactivation. */
    @PatchMapping("/{donorId}/reactivate")
    public ResponseEntity<ApiResponse<DonorResponse>> reactivateDonor(
            @PathVariable Long donorId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Donor reactivated",
                donorService.reactivateDonor(donorId, principal.getUsername())));
    }
}
