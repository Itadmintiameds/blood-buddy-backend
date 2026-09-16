package bloodbuddy.backend.dto.donor;

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
}
