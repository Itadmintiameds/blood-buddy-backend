package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.inventory.AddAvailabilityRequest;
import bloodbuddy.backend.dto.inventory.InventoryResponse;
import bloodbuddy.backend.dto.inventory.StockAdjustmentRequest;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.security.CustomUserDetails;
import bloodbuddy.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/add-availability")
    public ResponseEntity<ApiResponse<InventoryResponse>> addAvailability(
            @Valid @RequestBody AddAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long centreId = requireCentre(principal);
        InventoryResponse response = inventoryService.addAvailability(centreId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Availability updated", response));
    }

    @PostMapping("/stock-adjustment")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long centreId = requireCentre(principal);
        InventoryResponse response = inventoryService.adjustStock(centreId, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> listStock(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long centreId = requireCentre(principal);
        return ResponseEntity.ok(ApiResponse.success("Stock fetched", inventoryService.listCentreStock(centreId)));
    }

    // Staff may only touch their own centre; the centre id comes from the token, never the client.
    private Long requireCentre(CustomUserDetails principal) {
        if (principal.getBloodCentreId() == null) {
            throw new BadRequestException("This account is not linked to a blood centre");
        }
        return principal.getBloodCentreId();
    }
}
