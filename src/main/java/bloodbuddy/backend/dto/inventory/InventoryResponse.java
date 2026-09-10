package bloodbuddy.backend.dto.inventory;

import bloodbuddy.backend.entity.Inventory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponse {

    private final Long inventoryId;
    private final Long bloodGroupId;
    private final String bloodGroupName;
    private final Long bloodComponentId;
    private final String bloodComponentName;
    private final Long availableUnits;

    public static InventoryResponse fromEntity(Inventory inventory) {
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
