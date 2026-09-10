package bloodbuddy.backend.dto.inventory;

import bloodbuddy.backend.enums.StockMovement;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustmentRequest {

    @NotNull(message = "bloodGroupId is required")
    private Long bloodGroupId;

    @NotNull(message = "bloodComponentId is required")
    private Long bloodComponentId;

    @NotNull(message = "movement is required (ISSUE, DISCARD or CORRECTION)")
    private StockMovement movement;

    // Signed delta applied to available_units: negative for ISSUE/DISCARD,
    // and either sign (non-zero) for CORRECTION.
    @NotNull(message = "changedUnits is required")
    private Long changedUnits;

    private String remarks;
}
