package bloodbuddy.backend.dto.donor;

/**
 * Effective availability of a donor as exposed by the API (response status and list filter).
 * LOCKED is derived, not stored: an ACTIVE donor whose lock window contains today.
 */
public enum DonorAvailabilityStatus {
    ACTIVE,
    LOCKED,
    DEACTIVATED
}
