package bloodbuddy.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloseRequestRequest {

    /** Optional note on how the request was resolved. */
    private String remarks;
}
