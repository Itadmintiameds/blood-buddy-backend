package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Logout revokes every live token for the user so none can mint access tokens again.
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.userId = :userId AND r.revoked = false")
    int revokeAllForUser(@Param("userId") Long userId);
}
