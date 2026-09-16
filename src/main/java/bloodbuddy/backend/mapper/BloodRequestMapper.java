package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.request.BloodRequestDetailResponse;
import bloodbuddy.backend.dto.request.BloodRequestSummaryResponse;
import bloodbuddy.backend.entity.BloodRequest;

import java.util.List;

/** Maps {@link BloodRequest} entities to their response DTOs. */
public final class BloodRequestMapper {

    private BloodRequestMapper() {
    }

    /** Compact row for the superadmin blood-request list. */
    public static BloodRequestSummaryResponse toSummaryResponse(BloodRequest request) {
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

    /** Full superadmin view: recipient details, matched centres, donations, and donor candidates. */
    public static BloodRequestDetailResponse toDetailResponse(BloodRequest request,
                                                              List<BloodCentreResponse> matchedCentres,
                                                              List<DonorResponse> donatedBy,
                                                              List<DonorResponse> donorCandidates) {
        return BloodRequestDetailResponse.builder()
                .bloodRequestId(request.getBloodRequestId())
                .recipientName(request.getRecipientName())
                .mobileNumber(request.getMobileNumber())
                .bloodGroupId(request.getBloodGroup() != null ? request.getBloodGroup().getBloodGroupId() : null)
                .bloodGroupName(request.getBloodGroup() != null ? request.getBloodGroup().getBloodGroupName() : null)
                .bloodComponentId(request.getBloodComponent() != null ? request.getBloodComponent().getBloodComponentId() : null)
                .bloodComponentName(request.getBloodComponent() != null ? request.getBloodComponent().getBloodComponentName() : null)
                .requiredUnits(request.getRequiredUnits())
                .dob(request.getDob())
                .hospitalName(request.getHospitalName())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .pincode(request.getPincode())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .createdAt(request.getCreatedAt())
                .matchedCentres(matchedCentres)
                .donatedBy(donatedBy)
                .donorCandidates(donorCandidates)
                .build();
    }
}
