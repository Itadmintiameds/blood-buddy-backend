package bloodbuddy.backend.entity;

import java.util.Set;

/**
 * Reason a donor is unavailable. ILLNESS / OUT_OF_STATION are temporary (used when
 * locking); MEDICAL / DEATH / RELOCATED are permanent (used when deactivating).
 */
public enum DonorUnavailabilityReason {
    ILLNESS,
    OUT_OF_STATION,
    MEDICAL,
    DEATH,
    RELOCATED;

    /** Reasons valid for a temporary lock. */
    public static final Set<DonorUnavailabilityReason> LOCK_REASONS =
            Set.of(ILLNESS, OUT_OF_STATION);

    /** Reasons valid for a permanent deactivation. */
    public static final Set<DonorUnavailabilityReason> DEACTIVATION_REASONS =
            Set.of(MEDICAL, DEATH, RELOCATED);

    public boolean isLockReason() {
        return LOCK_REASONS.contains(this);
    }

    public boolean isDeactivationReason() {
        return DEACTIVATION_REASONS.contains(this);
    }
}
