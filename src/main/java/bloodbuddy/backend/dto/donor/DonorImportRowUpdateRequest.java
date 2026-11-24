package bloodbuddy.backend.dto.donor;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Corrected values for a staged row (replaces all of them). Values are free text like the CSV; the
 * row is re-validated after saving, so problems show up as row errors rather than a 400.
 */
@Getter
@Setter
public class DonorImportRowUpdateRequest {

    @Size(max = 255)
    private String fullName;

    @Size(max = 255)
    private String mobileNumber;

    @Size(max = 255)
    private String alternativeMobileNumber;

    // Blood group name, e.g. "O+".
    @Size(max = 255)
    private String bloodGroup;

    // YYYY-MM-DD (DD-MM-YYYY and DD/MM/YYYY are also accepted).
    @Size(max = 255)
    private String dob;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String district;

    @Size(max = 255)
    private String pincode;

    @Size(max = 255)
    private String lastBloodDonationDate;

    @Size(max = 255)
    private String remarks;
}
