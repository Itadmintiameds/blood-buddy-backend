package bloodbuddy.backend.dto.centre;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Superadmin edit of a blood centre: the centre-editable profile fields plus licence details.
 * Email (the login username) and active status are not editable here.
 */
@Getter
@Setter
public class BloodCentreUpdateRequest extends BloodCentreProfileUpdateRequest {

    private String bloodCentreLicenceNumber;

    private LocalDate licenceExpiryDate;
}
