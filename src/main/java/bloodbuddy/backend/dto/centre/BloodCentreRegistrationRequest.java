package bloodbuddy.backend.dto.centre;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BloodCentreRegistrationRequest {

    // --- Blood centre details ---
    @NotBlank(message = "bloodCentreName is required")
    private String bloodCentreName;

    private String bloodBankCategory;

    private String bloodCentreLicenceNumber;

    private LocalDate licenceExpiryDate;

    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;

    @NotBlank(message = "email is required")
    private String email;

    private String address;

    @NotBlank(message = "district is required")
    private String district;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "pincode is required")
    private String pincode;

    private Double latitude;

    private Double longitude;

    private String locationUrl;

    // --- Login credential for the centre's BLOOD_CENTRE account (username is the email above) ---
    @NotBlank(message = "password is required")
    private String password;
}
