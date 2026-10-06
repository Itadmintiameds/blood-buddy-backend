package bloodbuddy.backend.dto.common;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Distinct city/district values for a resource, used to populate filter dropdowns. */
@Getter
@Builder
public class LocationOptionsResponse {

    private final List<String> cities;
    private final List<String> districts;
}
