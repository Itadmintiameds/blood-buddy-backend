package bloodbuddy.backend.dto.centre;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodCentreRegistrationResponse {

    private final Long bloodCentreId;
    private final String bloodCentreName;
    private final String username;
    private final String email;
    private final String role;
}
