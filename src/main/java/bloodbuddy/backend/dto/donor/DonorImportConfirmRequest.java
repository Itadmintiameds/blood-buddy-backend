package bloodbuddy.backend.dto.donor;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonorImportConfirmRequest {

    // Superadmin attests that every donor being imported gave consent offline (paper form / camp).
    @AssertTrue(message = "Confirm that the donors in this file have given consent")
    private boolean consentConfirmed;

    // Version of the donor consent text shown alongside the attestation (e.g. "v1").
    @NotBlank(message = "Consent version is required")
    @Size(max = 20)
    private String consentVersion;
}
