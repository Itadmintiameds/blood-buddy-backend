package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.enums.DonorImportRowStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** A staged import row for the review table; values are the text as uploaded or edited. */
@Getter
@Builder
public class DonorImportRowResponse {

    private final Long rowId;
    private final Integer rowNumber;

    private final String fullName;
    private final String mobileNumber;
    private final String alternativeMobileNumber;
    private final String bloodGroup;
    private final String dob;
    private final String address;
    private final String city;
    private final String district;
    private final String pincode;
    private final String lastBloodDonationDate;
    private final String remarks;

    private final DonorImportRowStatus status;
    private final List<String> errors;
    private final Long existingDonorId;
    private final Integer duplicateOfRow;
    private final boolean excluded;
}
