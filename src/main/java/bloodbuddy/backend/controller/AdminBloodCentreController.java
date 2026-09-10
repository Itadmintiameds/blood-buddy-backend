package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.inventory.AddAvailabilityRequest;
import bloodbuddy.backend.dto.inventory.CentreInventoryResponse;
import bloodbuddy.backend.dto.inventory.StockAdjustmentRequest;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.BloodCentreService;
import bloodbuddy.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Superadmin views over all centres and any centre's stock (secured by /admin/** -> SUPERADMIN). */
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
