package bloodbuddy.backend.dto.donor;

import lombok.Builder;
import lombok.Getter;

/** Aggregate KPI figures for the superadmin donor dashboard. */
@Getter
@Builder
public class DonorStatsResponse {

    /** Total number of registered donors. */
    private final long totalDonors;

    /** Number of distinct blood groups represented across all donors. */
    private final long distinctBloodGroupCount;

    /** Donors whose last donation falls within the trailing 30 days. */
    private final long recentDonationCount;
}
