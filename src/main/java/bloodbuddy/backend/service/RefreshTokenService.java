package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.RefreshToken;
import bloodbuddy.backend.entity.Users;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiryMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiryMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public RefreshToken create(Users user) {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(now.plusNanos(refreshTokenExpiryMs * 1_000_000));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(now);
        return refreshTokenRepository.save(refreshToken);
    }

    /** A refresh token can mint an access token only while it is non-revoked and non-expired. */
    @Transactional(readOnly = true)
    public RefreshToken verifyUsable(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new BadRequestException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate() == null || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token has expired");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }
}
