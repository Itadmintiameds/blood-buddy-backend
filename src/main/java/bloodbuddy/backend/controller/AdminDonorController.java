package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.dto.donor.DonorFilterRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.donor.DonorStatsResponse;
import bloodbuddy.backend.service.DonorService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
}
