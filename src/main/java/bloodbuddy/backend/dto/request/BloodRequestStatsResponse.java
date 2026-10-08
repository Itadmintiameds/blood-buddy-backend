package bloodbuddy.backend.dto.request;

import lombok.Builder;
import lombok.Getter;

/** Aggregate KPI figures for the superadmin blood-request dashboard. */
@Getter
@Builder
public class BloodRequestStatsResponse {

    /** Total number of blood requests across all statuses. */
    private final long totalRequests;

    /** Active requests still being worked (CENTRES_FOUND or NO_CENTRES_FOUND). */
    private final long openRequests;

    /** Requests the admin has closed. */
    private final long closedRequests;

    /** Total units fulfilled across all closed requests. */
    private final long closedUnits;
}
