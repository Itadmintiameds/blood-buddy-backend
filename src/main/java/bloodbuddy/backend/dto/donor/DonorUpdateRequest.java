package bloodbuddy.backend.dto.donor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Superadmin edit of a donor's profile. Full replacement of the editable fields; availability
 * (status / lock / deactivation) is managed through the dedicated lock/deactivate/reactivate endpoints.
 */
@Getter
@Setter
public class DonorUpdateRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;

    private String alternativeMobileNumber;

    @NotNull(message = "bloodGroupId is required")
    private Long bloodGroupId;

    @PastOrPresent(message = "dob cannot be in the future")
    private LocalDate dob;

    private String address;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "district is required")
    private String district;

    @NotBlank(message = "pincode is required")
    private String pincode;

    @PastOrPresent(message = "lastBloodDonationDate cannot be in the future")
    private LocalDate lastBloodDonationDate;
}
