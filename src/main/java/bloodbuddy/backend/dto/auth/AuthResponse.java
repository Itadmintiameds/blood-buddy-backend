package bloodbuddy.backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
    private final String refreshToken;
    private final String username;
    private final String role;
    private final Long bloodCentreId;
    private final String bloodCentreName;
}
