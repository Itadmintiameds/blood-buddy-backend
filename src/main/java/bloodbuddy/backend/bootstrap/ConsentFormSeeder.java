package bloodbuddy.backend.bootstrap;

import bloodbuddy.backend.entity.ConsentForm;
import bloodbuddy.backend.enums.ConsentType;
import bloodbuddy.backend.repository.ConsentFormRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds v1 of each consent form only when no form of that type exists yet. Later versions are added
 * in the DB, never by editing these seeds — existing records point at the wording they accepted.
 */
@Component
@Order(1)
public class ConsentFormSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConsentFormSeeder.class);

    private static final List<ConsentSeed> SEEDS = List.of(
            // From the printed "Rotary BloodBuddy Voluntary Donor Consent Form".
            new ConsentSeed(ConsentType.DONOR,
                    "Rotary BloodBuddy Voluntary Donor Consent Form",
                    "I hereby voluntarily express my willingness to support blood donation activities in the future "
                            + "and to donate blood in critical needs, or requests for voluntary blood donation. "
                            + "I understand that Rotary BloodBuddy may contact me when there is an urgent requirement "
                            + "for this noble service of blood donation, subject to my availability and eligibility."),
            new ConsentSeed(ConsentType.RECIPIENT,
                    "Rotary BloodBuddy Blood Request Consent Form",
                    "I hereby confirm that the details provided in this blood request are true and correct to the best "
                            + "of my knowledge. I consent to Rotary BloodBuddy sharing these details with blood centres "
                            + "and voluntary donors in order to arrange the required blood, and to being contacted regarding "
                            + "this request. I understand that Rotary BloodBuddy only facilitates this request and does not "
                            + "guarantee the availability of blood or donors."));

    private final ConsentFormRepository consentFormRepository;

    public ConsentFormSeeder(ConsentFormRepository consentFormRepository) {
        this.consentFormRepository = consentFormRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        SEEDS.forEach(this::seed);
    }

    private void seed(ConsentSeed seed) {
        if (consentFormRepository.existsByConsentType(seed.type())) {
            return;
        }
        ConsentForm form = new ConsentForm();
        form.setConsentType(seed.type());
        form.setVersion("v1");
        form.setTitle(seed.title());
        form.setContent(seed.content());
        form.setIsActive(true);
        form.setCreatedAt(LocalDateTime.now());
        consentFormRepository.save(form);
        log.info("Seeded {} consent form v1", seed.type());
    }

    private record ConsentSeed(ConsentType type, String title, String content) {
    }
}
