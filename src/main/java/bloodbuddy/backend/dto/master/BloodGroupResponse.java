package bloodbuddy.backend.dto.master;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodGroupResponse {

    private final Long bloodGroupId;
    private final String bloodGroupName;
    private final Boolean isActive;
}
