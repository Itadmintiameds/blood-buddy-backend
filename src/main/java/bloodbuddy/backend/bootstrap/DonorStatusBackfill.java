package bloodbuddy.backend.bootstrap;

import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backfills the donor availability status for rows created before the feature existed.
 * Such donors have a null status; this marks them ACTIVE so they appear consistently in
 * status-filtered lists. Idempotent: once backfilled it updates nothing on later startups.
 */
@Component
@Order(2)
public class DonorStatusBackfill implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DonorStatusBackfill.class);

    private final BloodDonorDetailsRepository bloodDonorDetailsRepository;

    public DonorStatusBackfill(BloodDonorDetailsRepository bloodDonorDetailsRepository) {
        this.bloodDonorDetailsRepository = bloodDonorDetailsRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int updated = bloodDonorDetailsRepository.markNullStatusActive();
        if (updated > 0) {
            log.info("Backfilled status=ACTIVE for {} donor(s) with no status", updated);
        }
    }
}
