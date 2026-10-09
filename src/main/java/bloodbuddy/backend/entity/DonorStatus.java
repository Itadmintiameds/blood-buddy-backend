package bloodbuddy.backend.entity;

/**
 * Availability lifecycle of a donor.
 * ACTIVE: available for outreach. LOCKED: temporarily unavailable for a date range
 * (illness / out-of-station) and auto-reappears once the lock expires. DEACTIVATED:
 * permanently removed (medical grounds / deceased / relocated).
 */
public enum DonorStatus {
    ACTIVE,
    LOCKED,
    DEACTIVATED
}
