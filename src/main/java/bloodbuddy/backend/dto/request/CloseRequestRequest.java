package bloodbuddy.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloseRequestRequest {

    /** Optional note on how the request was resolved. */
    private String remarks;

    /** Approximate number of units fulfilled at the time of closing. */
    private Long closedUnits;
}
