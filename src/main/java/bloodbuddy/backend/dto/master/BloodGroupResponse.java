package bloodbuddy.backend.dto.master;

import bloodbuddy.backend.entity.masters.BloodGroup;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodGroupResponse {

    private final Long bloodGroupId;
    private final String bloodGroupName;
    private final Boolean isActive;

    public static BloodGroupResponse fromEntity(BloodGroup bloodGroup) {
        return BloodGroupResponse.builder()
                .bloodGroupId(bloodGroup.getBloodGroupId())
                .bloodGroupName(bloodGroup.getBloodGroupName())
                .isActive(bloodGroup.getIsActive())
                .build();
    }
}
