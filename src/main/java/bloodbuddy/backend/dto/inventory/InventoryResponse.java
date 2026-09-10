package bloodbuddy.backend.dto.inventory;

import bloodbuddy.backend.entity.Inventory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponse {

    private final Long inventoryId;
    private final Long bloodCentreId;
    private final Long bloodGroupId;
    private final Long bloodComponentId;
    private final Long availableUnits;

    public static InventoryResponse fromEntity(Inventory inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .bloodCentreId(inventory.getBloodCentre().getBloodCentreId())
                .bloodGroupId(inventory.getBloodGroup().getBloodGroupId())
                .bloodComponentId(inventory.getBloodComponent().getBloodComponentId())
                .availableUnits(inventory.getAvailableUnits())
                .build();
    }
}
