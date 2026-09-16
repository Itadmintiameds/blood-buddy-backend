package bloodbuddy.backend.dto.request;

import bloodbuddy.backend.entity.BloodRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Compact row for the superadmin blood-request list. */
@Getter
@Builder
public class BloodRequestSummaryResponse {

    private final Long bloodRequestId;
    private final String recipientName;
    private final String mobileNumber;
    private final String bloodGroupName;
    private final String bloodComponentName;
    private final Long requiredUnits;
    private final String city;
    private final String district;
    private final String pincode;
    private final BloodRequestStatus status;
    private final LocalDateTime createdAt;
}
