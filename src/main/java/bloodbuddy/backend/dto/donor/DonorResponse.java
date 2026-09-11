package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.entity.BloodDonorDetails;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DonorResponse {

    private final Long bloodDonorDetailsId;
    private final String fullName;
    private final String mobileNumber;
    private final String alternativeMobileNumber;
    private final Long bloodGroupId;
    private final String bloodGroupName;
    private final LocalDate dob;
    private final String address;
    private final String city;
    private final String district;
    private final String pincode;
    private final LocalDate lastBloodDonationDate;
    private final LocalDateTime createdAt;

    public static DonorResponse fromEntity(BloodDonorDetails donor) {
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
