package bloodbuddy.backend.dto.centre;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {

    @NotBlank(message = "email is required")
    @Email(message = "a valid email is required")
    private String email;
}
