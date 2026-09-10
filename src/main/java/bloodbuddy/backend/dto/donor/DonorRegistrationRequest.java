package bloodbuddy.backend.dto.donor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DonorRegistrationRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;

    private String alternativeMobileNumber;

    @NotNull(message = "bloodGroupId is required")
    private Long bloodGroupId;

    private LocalDate dob;

    private String address;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "district is required")
    private String district;

    @NotBlank(message = "pincode is required")
    private String pincode;

    private LocalDate lastBloodDonationDate;
}
