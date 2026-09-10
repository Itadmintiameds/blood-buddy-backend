package bloodbuddy.backend.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodRequestResponse {

    private final Long bloodRequestId;
    private final boolean matched;
    private final int matchedCentreCount;
    private final String message;
}
