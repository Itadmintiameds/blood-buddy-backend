package bloodbuddy.backend.dto.request;

import bloodbuddy.backend.entity.BloodRequest;
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

    public static BloodRequestSummaryResponse fromEntity(BloodRequest request) {
        return BloodRequestSummaryResponse.builder()
                .bloodRequestId(request.getBloodRequestId())
                .recipientName(request.getRecipientName())
                .mobileNumber(request.getMobileNumber())
                .bloodGroupName(request.getBloodGroup() != null ? request.getBloodGroup().getBloodGroupName() : null)
                .bloodComponentName(request.getBloodComponent() != null ? request.getBloodComponent().getBloodComponentName() : null)
                .requiredUnits(request.getRequiredUnits())
                .city(request.getCity())
                .district(request.getDistrict())
                .pincode(request.getPincode())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
