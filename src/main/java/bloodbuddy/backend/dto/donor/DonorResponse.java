package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.entity.BloodDonorDetails;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DonorResponse {

    private final Long bloodDonorDetailsId;
    private final String fullName;
    private final String mobileNumber;
    private final Long bloodGroupId;
    private final String city;
    private final String district;
    private final String pincode;

    public static DonorResponse fromEntity(BloodDonorDetails donor) {
        return DonorResponse.builder()
                .bloodDonorDetailsId(donor.getBloodDonorDetailsId())
                .fullName(donor.getFullName())
                .mobileNumber(donor.getMobileNumber())
                .bloodGroupId(donor.getBloodGroup() != null ? donor.getBloodGroup().getBloodGroupId() : null)
                .city(donor.getCity())
                .district(donor.getDistrict())
                .pincode(donor.getPincode())
                .build();
    }
}
