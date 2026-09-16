package bloodbuddy.backend.dto.master;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodComponentResponse {

    private final Long bloodComponentId;
    private final String bloodComponentName;
    private final Integer shelfLifeDays;
    private final Boolean isActive;
}
