package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.master.BloodComponentResponse;
import bloodbuddy.backend.entity.masters.BloodComponents;

/** Maps {@link BloodComponents} entities to their response DTOs. */
public final class BloodComponentMapper {

    private BloodComponentMapper() {
    }

    public static BloodComponentResponse toResponse(BloodComponents bloodComponent) {
        return BloodComponentResponse.builder()
                .bloodComponentId(bloodComponent.getBloodComponentId())
                .bloodComponentName(bloodComponent.getBloodComponentName())
                .shelfLifeDays(bloodComponent.getShelfLifeDays())
                .isActive(bloodComponent.getIsActive())
                .build();
    }
}
