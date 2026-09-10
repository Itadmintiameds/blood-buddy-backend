package bloodbuddy.backend.dto.centre;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

    @NotBlank(message = "email is required")
    @Email(message = "a valid email is required")
    private String email;

    @NotBlank(message = "otp is required")
    private String otp;
}
