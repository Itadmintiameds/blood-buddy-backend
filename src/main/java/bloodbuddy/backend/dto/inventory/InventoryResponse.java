package bloodbuddy.backend.dto.inventory;

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
}
