package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.inventory.InventoryResponse;
import bloodbuddy.backend.entity.Inventory;

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
}
