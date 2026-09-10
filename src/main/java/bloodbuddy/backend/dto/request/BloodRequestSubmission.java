package bloodbuddy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BloodRequestSubmission {

    @NotBlank(message = "recipientName is required")
    private String recipientName;

    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;

    @NotNull(message = "bloodComponentId is required")
    private Long bloodComponentId;

    @NotNull(message = "bloodGroupId is required")
    private Long bloodGroupId;

    @NotNull(message = "requiredUnits is required")
    @Positive(message = "requiredUnits must be greater than zero")
    private Long requiredUnits;

    private LocalDate dob;

    private String hospitalName;

    private String address;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "district is required")
    private String district;

    @NotBlank(message = "pincode is required")
    private String pincode;
}
