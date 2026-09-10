package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.inventory.AddAvailabilityRequest;
import bloodbuddy.backend.dto.inventory.CentreInventoryResponse;
import bloodbuddy.backend.dto.inventory.StockAdjustmentRequest;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Blood-centre staff manage their own centre's stock; the centre comes from the token.
 *  Superadmin operates on any centre via /admin/blood-centres/{id}/inventory/*. */
@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasRole('BLOOD_CENTRE')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/add-availability")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> addAvailability(
            @Valid @RequestBody AddAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CentreInventoryResponse response = inventoryService.addAvailability(ownCentre(principal), request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Availability updated", response));
    }

    @PostMapping("/stock-adjustment")
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CentreInventoryResponse response = inventoryService.adjustStock(ownCentre(principal), request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CentreInventoryResponse>> listStock(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success("Stock fetched",
                inventoryService.getCentreInventory(ownCentre(principal))));
    }

    // Staff may only touch their own centre; the centre id comes from the token, never the client.
    private Long ownCentre(CustomUserDetails principal) {
        if (principal.getBloodCentreId() == null) {
            throw new BadRequestException("This account is not linked to a blood centre");
        }
        return principal.getBloodCentreId();
    }
}
