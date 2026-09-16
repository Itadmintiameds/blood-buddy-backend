package bloodbuddy.backend.dto.centre;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodCentreResponse {

    private final Long bloodCentreId;
    private final String bloodCentreName;
    private final String bloodBankCategory;
    private final String mobileNumber;
    private final String email;
    private final String address;
    private final String district;
    private final String city;
    private final String pincode;
    private final Double latitude;
    private final Double longitude;
    private final String locationUrl;
    private final Boolean isActive;
}
