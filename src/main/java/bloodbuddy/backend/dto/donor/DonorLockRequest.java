package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.entity.DonorUnavailabilityReason;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Superadmin request to put a donor on a temporary lock-in period. The reason must be a
 * lock reason (ILLNESS / OUT_OF_STATION); further cross-field validation (reason category,
 * lockedUntil not before lockedFrom) is enforced in the service.
 */
@Getter
@Setter
public class DonorLockRequest {

    @NotNull(message = "reason is required")
    private DonorUnavailabilityReason reason;

    /** Start of the lock window. Defaults to today when omitted. */
    @FutureOrPresent(message = "lockedFrom cannot be in the past")
    private LocalDate lockedFrom;

    @NotNull(message = "lockedUntil is required")
    @FutureOrPresent(message = "lockedUntil cannot be in the past")
    private LocalDate lockedUntil;

    /** Optional free-text note (e.g. "No answer, travelling"). */
    private String remarks;
}
