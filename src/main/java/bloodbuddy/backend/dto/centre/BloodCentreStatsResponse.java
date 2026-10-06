package bloodbuddy.backend.dto.centre;

import lombok.Builder;
import lombok.Getter;

/** Aggregate KPI figures for the superadmin blood-centre dashboard. */
@Getter
@Builder
public class BloodCentreStatsResponse {

    /** Count of active blood centres. */
    private final long totalBloodCentres;

    /**
     * Active centres needing attention: at least one inventory item at/below the low-stock
     * threshold (&le; 3 units, which includes 0/out-of-stock) OR no stock at all.
     */
    private final long lowStockCentres;

    /** Sum of available units across all inventory. */
    private final long totalBloodUnits;
}
