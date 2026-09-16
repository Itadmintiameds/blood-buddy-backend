package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.master.BloodGroupResponse;
import bloodbuddy.backend.entity.masters.BloodGroup;

/** Maps {@link BloodGroup} entities to their response DTOs. */
public final class BloodGroupMapper {

    private BloodGroupMapper() {
    }

    public static BloodGroupResponse toResponse(BloodGroup bloodGroup) {
        return BloodGroupResponse.builder()
                .bloodGroupId(bloodGroup.getBloodGroupId())
                .bloodGroupName(bloodGroup.getBloodGroupName())
                .isActive(bloodGroup.getIsActive())
                .build();
    }
}