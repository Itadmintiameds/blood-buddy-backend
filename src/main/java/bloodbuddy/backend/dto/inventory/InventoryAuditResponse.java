package bloodbuddy.backend.dto.inventory;

import bloodbuddy.backend.enums.StockMovement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** A single stock-movement entry from the inventory ledger, carrying group/component names. */
@Getter
@Builder
public class InventoryAuditResponse {

    private final Long inventoryAuditId;
    private final Long inventoryId;
    private final Long bloodGroupId;
    private final String bloodGroupName;
    private final Long bloodComponentId;
    private final String bloodComponentName;
    private final StockMovement stockMovement;
    private final Long changedUnits;
    private final Long remainingUnits;
    private final String remarks;
    private final LocalDateTime createdAt;
    private final String createdBy;
}
