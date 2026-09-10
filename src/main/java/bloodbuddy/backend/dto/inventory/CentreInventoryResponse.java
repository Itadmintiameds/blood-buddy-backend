package bloodbuddy.backend.dto.inventory;

import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** A centre's full details together with its stock (each item carrying group/component names). */
@Getter
@Builder
public class CentreInventoryResponse {

    private final BloodCentreResponse bloodCentre;
    private final List<InventoryResponse> inventory;
}
