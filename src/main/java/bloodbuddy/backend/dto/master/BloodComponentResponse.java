package bloodbuddy.backend.dto.master;

import bloodbuddy.backend.entity.masters.BloodComponents;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodComponentResponse {

    private final Long bloodComponentId;
    private final String bloodComponentName;
    private final Integer shelfLifeDays;
    private final Boolean isActive;

    public static BloodComponentResponse fromEntity(BloodComponents bloodComponent) {
        return BloodComponentResponse.builder()
                .bloodComponentId(bloodComponent.getBloodComponentId())
                .bloodComponentName(bloodComponent.getBloodComponentName())
                .shelfLifeDays(bloodComponent.getShelfLifeDays())
                .isActive(bloodComponent.getIsActive())
                .build();
    }
}
