package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;

/** Maps {@link BloodDonorDetails} entities to their response DTOs. */
public final class DonorMapper {

    private DonorMapper() {
    }

    public static DonorResponse toResponse(BloodDonorDetails donor) {
        return DonorResponse.builder()
                .bloodDonorDetailsId(donor.getBloodDonorDetailsId())
                .fullName(donor.getFullName())
                .mobileNumber(donor.getMobileNumber())
                .alternativeMobileNumber(donor.getAlternativeMobileNumber())
                .bloodGroupId(donor.getBloodGroup() != null ? donor.getBloodGroup().getBloodGroupId() : null)
                .bloodGroupName(donor.getBloodGroup() != null ? donor.getBloodGroup().getBloodGroupName() : null)
                .dob(donor.getDob())
                .address(donor.getAddress())
                .city(donor.getCity())
                .district(donor.getDistrict())
                .pincode(donor.getPincode())
                .lastBloodDonationDate(donor.getLastBloodDonationDate())
                .createdAt(donor.getCreatedAt())
                .build();
    }
}
