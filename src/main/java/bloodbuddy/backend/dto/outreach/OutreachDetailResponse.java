package bloodbuddy.backend.dto.outreach;

import bloodbuddy.backend.entity.BloodRequestDetails;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OutreachDetailResponse {

    private final Long bloodRequestDetailsId;
    private final Long bloodRequestId;
    private final Long bloodCentreId;
    private final Long bloodDonorDetailsId;
    private final String status;
    private final String remarks;

    public static OutreachDetailResponse fromEntity(BloodRequestDetails details) {
        return OutreachDetailResponse.builder()
                .bloodRequestDetailsId(details.getBloodRequestDetailsId())
                .bloodRequestId(details.getBloodRequest() != null ? details.getBloodRequest().getBloodRequestId() : null)
                .bloodCentreId(details.getBloodCentre() != null ? details.getBloodCentre().getBloodCentreId() : null)
                .bloodDonorDetailsId(details.getBloodDonorDetails() != null
                        ? details.getBloodDonorDetails().getBloodDonorDetailsId() : null)
                .status(details.getStatus())
                .remarks(details.getRemarks())
                .build();
    }
}
