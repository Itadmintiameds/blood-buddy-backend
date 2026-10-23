package bloodbuddy.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Superadmin edit of a blood request. Full replacement of the editable fields. Changing a
 * matching input (blood group, component or location) on an open request re-runs centre
 * matching; status/closure is otherwise managed through the close endpoint.
 */
@Getter
@Setter
public class BloodRequestUpdateRequest {

    @NotBlank(message = "recipientName is required")
    private String recipientName;

    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;

    @NotNull(message = "bloodGroupId is required")
    private Long bloodGroupId;

    @NotNull(message = "bloodComponentId is required")
    private Long bloodComponentId;

    @NotNull(message = "requiredUnits is required")
    @Positive(message = "requiredUnits must be greater than zero")
    private Long requiredUnits;

    @Positive(message = "age must be greater than zero")
    private Integer age;

    private String hospitalName;

    private String address;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "district is required")
    private String district;

    private String pincode;
}
