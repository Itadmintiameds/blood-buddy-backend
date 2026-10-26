package bloodbuddy.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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

    // private LocalDate dob;
    private Integer age;

    private String hospitalName;

    private String address;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "district is required")
    private String district;

    // Optional: matching falls back to city/district when it is absent.
    private String pincode;

    // Consent checkbox on the form; the submission is rejected unless it is ticked.
    @AssertTrue(message = "Consent must be accepted")
    private boolean consentAccepted;

    // Version of the consent text shown on the form (e.g. "v1"), so we know exactly what was agreed to.
    @NotBlank(message = "Consent version is required")
    @Size(max = 20)
    private String consentVersion;
}
