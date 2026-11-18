package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.inventory.AddAvailabilityRequest;
import bloodbuddy.backend.dto.inventory.CentreInventoryResponse;
import bloodbuddy.backend.dto.inventory.InventoryAuditResponse;
import bloodbuddy.backend.dto.inventory.StockAdjustmentRequest;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stock of one blood centre. SUPERADMIN may act on any centre; BLOOD_CENTRE staff only on
 * their own, so the {bloodCentreId} in the path must match the centre in their token (else 403).
 */
@RestController
@RequestMapping("/blood-centres/{bloodCentreId}/inventory")
@PreAuthorize("hasRole('SUPERADMIN') or (hasRole('BLOOD_CENTRE') and #bloodCentreId == principal.bloodCentreId)")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> listStock(@PathVariable Long bloodCentreId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory fetched",
                inventoryService.getCentreInventory(bloodCentreId)));
    }

    /** Stock movement ledger for a single inventory row (newest first), paginated. */
    @GetMapping("/{inventoryId}/history")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryAuditResponse>>> stockHistory(
            @PathVariable Long bloodCentreId,
            @PathVariable Long inventoryId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Stock history fetched",
                inventoryService.getInventoryHistory(bloodCentreId, inventoryId, pageable)));
    }

    @PostMapping("/add-availability")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> addAvailability(
            @PathVariable Long bloodCentreId,
            @Valid @RequestBody AddAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CentreInventoryResponse response = inventoryService.addAvailability(bloodCentreId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Availability updated", response));
    }

    @PostMapping("/stock-adjustment")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> adjustStock(
            @PathVariable Long bloodCentreId,
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CentreInventoryResponse response = inventoryService.adjustStock(bloodCentreId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", response));
    }
}
