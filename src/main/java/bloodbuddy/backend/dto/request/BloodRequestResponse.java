package bloodbuddy.backend.dto.request;

import bloodbuddy.backend.entity.BloodRequestStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodRequestResponse {

    private final Long bloodRequestId;
    private final boolean matched;
    private final int matchedCentreCount;
    private final BloodRequestStatus status;
    private final String message;
}
