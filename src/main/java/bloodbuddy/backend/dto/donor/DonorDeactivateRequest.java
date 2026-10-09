package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.entity.DonorUnavailabilityReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Superadmin request to permanently deactivate a donor. The reason must be a deactivation
 * reason (MEDICAL / DEATH / RELOCATED); that category check is enforced in the service.
 */
@Getter
@Setter
public class DonorDeactivateRequest {

    @NotNull(message = "reason is required")
    private DonorUnavailabilityReason reason;

    /** Optional free-text note. */
    private String remarks;
}
