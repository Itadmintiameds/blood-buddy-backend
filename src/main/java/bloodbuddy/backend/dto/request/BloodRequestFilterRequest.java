package bloodbuddy.backend.dto.request;

import bloodbuddy.backend.entity.BloodRequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Optional filters for the superadmin blood-request listing. Every field is nullable/empty-friendly:
 * a null or empty value means "do not filter on this dimension". Bound from query parameters.
 */
@Getter
@Setter
public class BloodRequestFilterRequest {

    /** Match requests in any of these lifecycle statuses. */
    private List<BloodRequestStatus> statuses;

    /** Match requests for any of these blood groups. */
    private List<Long> bloodGroupIds;

    /** Match requests for any of these blood components. */
    private List<Long> bloodComponentIds;

    /** Match requests located in any of these cities (exact, case-insensitive). */
    private List<String> cities;

    /** Match requests located in any of these districts (exact, case-insensitive). */
    private List<String> districts;

    /** Free-text search across recipient name, mobile, hospital, address, city, district and pincode. */
    private String search;
}
