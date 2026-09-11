package bloodbuddy.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordDonationRequest {

    @NotNull(message = "bloodDonorDetailsId is required")
    private Long bloodDonorDetailsId;
}
