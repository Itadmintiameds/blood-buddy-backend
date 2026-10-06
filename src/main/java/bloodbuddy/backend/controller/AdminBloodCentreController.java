package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.centre.BloodCentreFilterRequest;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.centre.BloodCentreStatsResponse;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.dto.inventory.AddAvailabilityRequest;
import bloodbuddy.backend.dto.inventory.CentreInventoryResponse;
import bloodbuddy.backend.dto.inventory.StockAdjustmentRequest;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.BloodCentreService;
import bloodbuddy.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Superadmin views over all centres and any centre's stock (secured by /admin/** -> SUPERADMIN).
 */
@RestController
@RequestMapping("/admin/blood-centres")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminBloodCentreController {

    private final BloodCentreService bloodCentreService;
    private final InventoryService inventoryService;

    public AdminBloodCentreController(BloodCentreService bloodCentreService,
                                      InventoryService inventoryService) {
        this.bloodCentreService = bloodCentreService;
        this.inventoryService = inventoryService;
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

    @GetMapping("/{bloodCentreId}/inventory")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> centreInventory(
            @PathVariable Long bloodCentreId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory fetched",
                inventoryService.getCentreInventory(bloodCentreId)));
    }

    // Superadmin updates any centre's stock; the target centre comes from the path.
    @PostMapping("/{bloodCentreId}/inventory/add-availability")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> addAvailability(
            @PathVariable Long bloodCentreId,
            @Valid @RequestBody AddAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CentreInventoryResponse response = inventoryService.addAvailability(bloodCentreId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Availability updated", response));
    }

    @PostMapping("/{bloodCentreId}/inventory/stock-adjustment")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> adjustStock(
            @PathVariable Long bloodCentreId,
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CentreInventoryResponse response = inventoryService.adjustStock(bloodCentreId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", response));
    }
}
