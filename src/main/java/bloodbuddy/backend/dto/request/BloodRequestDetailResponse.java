package bloodbuddy.backend.dto.request;

import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.entity.BloodRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Full superadmin view of a request: recipient details, matched centres, donations, and — when no
 *  centre matched — the live donor candidates the admin can reach out to. */
@Getter
@Builder
public class BloodRequestDetailResponse {

    private final Long bloodRequestId;
    private final String recipientName;
    private final String mobileNumber;
    private final Long bloodGroupId;
    private final String bloodGroupName;
    private final Long bloodComponentId;
    private final String bloodComponentName;
    private final Long requiredUnits;
    private final LocalDate dob;
    private final String hospitalName;
    private final String address;
    private final String city;
    private final String district;
    private final String pincode;
    private final BloodRequestStatus status;
    private final String remarks;
    private final LocalDateTime createdAt;

    private final List<BloodCentreResponse> matchedCentres;
    private final List<DonorResponse> donatedBy;
    private final List<DonorResponse> donorCandidates;
}
