package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.auth.AuthResponse;
import bloodbuddy.backend.dto.auth.LoginRequest;
import bloodbuddy.backend.entity.RefreshToken;
import bloodbuddy.backend.entity.Users;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.repository.UsersRepository;
import bloodbuddy.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UsersRepository usersRepository,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.usersRepository = usersRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Delegates to DaoAuthenticationProvider: BCrypt-compares against the stored hash.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Users user = usersRepository.findByUsernameWithDetails(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        user.setLastLogin(LocalDateTime.now());
        RefreshToken refreshToken = refreshTokenService.create(user);

        return buildResponse(user, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.verifyUsable(refreshTokenValue);
        Users user = refreshToken.getUser();
        // Same refresh token is returned; only the access token is minted anew.
        return buildResponse(user, refreshToken.getToken());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private AuthResponse buildResponse(Users user, String refreshTokenValue) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .refreshToken(refreshTokenValue)
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .bloodCentreId(user.getBloodCentre() != null ? user.getBloodCentre().getBloodCentreId() : null)
                .build();
    }
}
