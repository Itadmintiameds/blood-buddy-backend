package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.request.BloodRequestDetailResponse;
import bloodbuddy.backend.dto.request.BloodRequestSummaryResponse;
import bloodbuddy.backend.dto.request.CloseRequestRequest;
import bloodbuddy.backend.dto.request.RecordDonationRequest;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.AdminBloodRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Superadmin views and actions over recipient blood requests (secured by /admin/** -> SUPERADMIN). */
@RestController
@RequestMapping("/admin/blood-requests")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminBloodRequestController {

    private final AdminBloodRequestService adminBloodRequestService;

    public AdminBloodRequestController(AdminBloodRequestService adminBloodRequestService) {
        this.adminBloodRequestService = adminBloodRequestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BloodRequestSummaryResponse>>> listRequests() {
        return ResponseEntity.ok(ApiResponse.success("Blood requests fetched",
                adminBloodRequestService.listAll()));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<BloodRequestSummaryResponse>>> listRequestsPaginated(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Blood requests fetched",
                adminBloodRequestService.list(pageable)));
    }

    @GetMapping("/{bloodRequestId}")
    public ResponseEntity<ApiResponse<BloodRequestDetailResponse>> requestDetail(
            @PathVariable Long bloodRequestId) {
        return ResponseEntity.ok(ApiResponse.success("Blood request fetched",
                adminBloodRequestService.getDetail(bloodRequestId)));
    }

    @PostMapping("/{bloodRequestId}/donation")
    public ResponseEntity<ApiResponse<BloodRequestDetailResponse>> recordDonation(
            @PathVariable Long bloodRequestId,
            @Valid @RequestBody RecordDonationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        BloodRequestDetailResponse response = adminBloodRequestService.recordDonation(
                bloodRequestId, request.getBloodDonorDetailsId(), principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Donation recorded", response));
    }

    @PatchMapping("/{bloodRequestId}/close")
    public ResponseEntity<ApiResponse<BloodRequestDetailResponse>> closeRequest(
            @PathVariable Long bloodRequestId,
            @RequestBody(required = false) CloseRequestRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        String remarks = request != null ? request.getRemarks() : null;
        BloodRequestDetailResponse response = adminBloodRequestService.close(
                bloodRequestId, remarks, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Request closed", response));
    }
}
