package bloodbuddy.backend.entity;

/**
 * Stored availability lifecycle of a donor.
 * ACTIVE: in the donor pool. DEACTIVATED: permanently removed (medical grounds / deceased /
 * relocated). A temporary lock is not a stored status: it is the lockedFrom / lockedUntil
 * window on an ACTIVE donor, so it ends on its own once lockedUntil passes.
 */
public enum DonorStatus {
    ACTIVE,
    DEACTIVATED
}
