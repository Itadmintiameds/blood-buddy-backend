package bloodbuddy.backend.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAvailabilityRequest {

    @NotNull(message = "bloodGroupId is required")
    private Long bloodGroupId;

    @NotNull(message = "bloodComponentId is required")
    private Long bloodComponentId;

    @NotNull(message = "units is required")
    @Positive(message = "units must be greater than zero")
    private Long units;

    private String remarks;
}
