package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.EmailVerification;
import bloodbuddy.backend.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndPurpose(String email, VerificationPurpose purpose);
}
