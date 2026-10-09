package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.entity.DonorStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Optional filters for the superadmin donor listing. Every field is nullable/empty-friendly:
 * a null or empty value means "do not filter on this dimension". Bound from query parameters.
 */
@Getter
@Setter
public class DonorFilterRequest {

    /** Match donors with any of these blood groups. */
    private List<Long> bloodGroupIds;

    /** Match donors located in any of these cities (exact, case-insensitive). */
    private List<String> cities;

    /** Match donors located in any of these districts (exact, case-insensitive). */
    private List<String> districts;

    /** Free-text search across name, mobile, alternative mobile, address, city, district and pincode. */
    private String search;

    /**
     * Match donors with any of these availability statuses. When empty, only currently
     * available donors are returned (deactivated and actively-locked donors are hidden).
     */
    private List<DonorStatus> statuses;
}
