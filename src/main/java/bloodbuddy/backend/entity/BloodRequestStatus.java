package bloodbuddy.backend.entity;

/** Lifecycle of a blood request. Set at submission; the closed states and CANCELLED are admin actions. */
public enum BloodRequestStatus {
    CENTRES_FOUND,
    NO_CENTRES_FOUND,
    /** Fully fulfilled: closed units met the required units. */
    CLOSED,
    /** Closed with fewer units fulfilled than required. */
    PARTIALLY_CLOSED,
    CANCELLED
}
