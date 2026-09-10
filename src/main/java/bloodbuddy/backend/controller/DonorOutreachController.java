package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.outreach.CreateOutreachRequest;
import bloodbuddy.backend.dto.outreach.OutreachDetailResponse;
import bloodbuddy.backend.dto.outreach.UpdateOutreachRequest;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.DonorOutreachService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/outreach")
@PreAuthorize("hasRole('SUPERADMIN')")
public class DonorOutreachController {

    private final DonorOutreachService donorOutreachService;

    public DonorOutreachController(DonorOutreachService donorOutreachService) {
        this.donorOutreachService = donorOutreachService;
    }

    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<DonorResponse>>> candidates(@RequestParam Long requestId) {
        return ResponseEntity.ok(ApiResponse.success("Candidate donors fetched",
                donorOutreachService.findCandidates(requestId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OutreachDetailResponse>>> details(@RequestParam Long requestId) {
        return ResponseEntity.ok(ApiResponse.success("Request details fetched",
                donorOutreachService.listDetails(requestId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OutreachDetailResponse>> create(
            @Valid @RequestBody CreateOutreachRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        OutreachDetailResponse response = donorOutreachService.createOutreach(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Donor outreach recorded", response));
    }

    @PatchMapping("/{detailsId}")
    public ResponseEntity<ApiResponse<OutreachDetailResponse>> update(
            @PathVariable Long detailsId,
            @Valid @RequestBody UpdateOutreachRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        OutreachDetailResponse response = donorOutreachService.updateOutreach(detailsId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Outreach updated", response));
    }
}
