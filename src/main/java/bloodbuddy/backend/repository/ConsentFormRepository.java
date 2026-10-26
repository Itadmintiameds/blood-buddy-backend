package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.ConsentForm;
import bloodbuddy.backend.enums.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentFormRepository extends JpaRepository<ConsentForm, Long> {

    Optional<ConsentForm> findFirstByConsentTypeAndIsActiveTrueOrderByCreatedAtDesc(ConsentType consentType);

    boolean existsByConsentType(ConsentType consentType);
}
