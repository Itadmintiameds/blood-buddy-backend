package bloodbuddy.backend.dto.outreach;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOutreachRequest {

    @NotNull(message = "bloodRequestId is required")
    private Long bloodRequestId;

    @NotNull(message = "bloodDonorDetailsId is required")
    private Long bloodDonorDetailsId;

    private String status;

    private String remarks;
}
