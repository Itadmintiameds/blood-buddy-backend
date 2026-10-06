package bloodbuddy.backend.dto.centre;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Optional filters for the superadmin blood-centre listing. Every field is nullable/empty-friendly:
 * a null or empty value means "do not filter on this dimension". Bound from query parameters.
 */
@Getter
@Setter
public class BloodCentreFilterRequest {

    /** Active/inactive status; null means both. */
    private Boolean isActive;

    /** Match centres located in any of these cities (exact, case-insensitive). */
    private List<String> cities;

    /** Match centres located in any of these districts (exact, case-insensitive). */
    private List<String> districts;

    /** Match centres that stock any of these blood groups (with units in stock). */
    private List<Long> bloodGroupIds;

    /** Match centres that stock any of these blood components (with units in stock). */
    private List<Long> bloodComponentIds;

    /** Free-text search across name, email, mobile, address, city, district, pincode and licence number. */
    private String search;
}
