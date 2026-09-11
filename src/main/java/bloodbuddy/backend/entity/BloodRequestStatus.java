package bloodbuddy.backend.entity;

/** Lifecycle of a blood request. Set at submission; CLOSED/CANCELLED are admin actions. */
public enum BloodRequestStatus {
    CENTRES_FOUND,
    NO_CENTRES_FOUND,
    CLOSED,
    CANCELLED
}
