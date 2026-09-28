package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.inventory.InventoryAuditResponse;
import bloodbuddy.backend.dto.inventory.InventoryResponse;
import bloodbuddy.backend.entity.Inventory;
import bloodbuddy.backend.entity.InventoryAudit;

/** Maps {@link Inventory} entities to their response DTOs. */
public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .bloodGroupId(inventory.getBloodGroup().getBloodGroupId())
                .bloodGroupName(inventory.getBloodGroup().getBloodGroupName())
                .bloodComponentId(inventory.getBloodComponent().getBloodComponentId())
                .bloodComponentName(inventory.getBloodComponent().getBloodComponentName())
                .availableUnits(inventory.getAvailableUnits())
                .build();
    }

    public static InventoryAuditResponse toAuditResponse(InventoryAudit audit) {
        Inventory inventory = audit.getInventory();
        return InventoryAuditResponse.builder()
                .inventoryAuditId(audit.getInventoryAuditId())
                .inventoryId(inventory.getInventoryId())
                .bloodGroupId(inventory.getBloodGroup().getBloodGroupId())
                .bloodGroupName(inventory.getBloodGroup().getBloodGroupName())
                .bloodComponentId(inventory.getBloodComponent().getBloodComponentId())
                .bloodComponentName(inventory.getBloodComponent().getBloodComponentName())
                .stockMovement(audit.getStockMovement())
                .changedUnits(audit.getChangedUnits())
                .remainingUnits(audit.getRemainingUnits())
                .remarks(audit.getRemarks())
                .createdAt(audit.getCreatedAt())
                .createdBy(audit.getCreatedBy())
                .build();
    }
}
