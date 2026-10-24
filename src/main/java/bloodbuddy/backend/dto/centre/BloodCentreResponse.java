package bloodbuddy.backend.dto.centre;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BloodCentreResponse {

    private final Long bloodCentreId;
    private final String bloodCentreName;
    // private final String bloodBankCategory;
    private final String mobileNumber;
    private final String email;
    private final String address;
    private final String district;
    private final String city;
    private final String pincode;
    private final Double latitude;
    private final Double longitude;
    private final String locationUrl;
    // Licence details: shown on the profile and pre-filled in the superadmin edit form
    // (PUT /admin/blood-centres/{id} is a full replacement, so they must round-trip).
    private final String bloodCentreLicenceNumber;
    private final LocalDate licenceExpiryDate;
    private final Boolean isActive;
}
