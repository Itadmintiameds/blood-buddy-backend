package bloodbuddy.backend.dto.centre;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Centre's own edit of its profile. Full replacement of the editable fields; email (the login
 * username), licence details and active status are not editable here.
 */
@Getter
@Setter
public class BloodCentreProfileUpdateRequest {

    @NotBlank(message = "bloodCentreName is required")
    private String bloodCentreName;

    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;

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
}
