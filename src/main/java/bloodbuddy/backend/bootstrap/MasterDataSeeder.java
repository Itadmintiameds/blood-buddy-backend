package bloodbuddy.backend.bootstrap;

import bloodbuddy.backend.entity.masters.BloodComponents;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.repository.BloodComponentsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the blood-group and blood-component reference data if absent. Idempotent, so it
 * re-seeds after ddl-auto: create wipes the tables on restart. Without this, recipient
 * requests and add-availability calls 404 on missing groups/components.
 */
@Component
@Order(1)
public class MasterDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MasterDataSeeder.class);

    private static final List<String> BLOOD_GROUPS =
            List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    // component name -> shelf life in days (informational; no expiry job runs yet)
    private static final List<BloodComponentSeed> BLOOD_COMPONENTS = List.of(
            new BloodComponentSeed("Whole Blood", 35),
            new BloodComponentSeed("Packed Red Blood Cells", 42),
            new BloodComponentSeed("Fresh Frozen Plasma", 365),
            new BloodComponentSeed("Platelet Concentrate", 5),
            new BloodComponentSeed("Cryoprecipitate", 365));

    private final BloodGroupRepository bloodGroupRepository;
    private final BloodComponentsRepository bloodComponentsRepository;

    public MasterDataSeeder(BloodGroupRepository bloodGroupRepository,
                            BloodComponentsRepository bloodComponentsRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.bloodComponentsRepository = bloodComponentsRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        BLOOD_GROUPS.forEach(this::seedGroup);
        BLOOD_COMPONENTS.forEach(this::seedComponent);
    }

    private void seedGroup(String name) {
        if (bloodGroupRepository.existsByBloodGroupName(name)) {
            return;
        }
        BloodGroup group = new BloodGroup();
        group.setBloodGroupName(name);
        group.setIsActive(true);
        bloodGroupRepository.save(group);
        log.info("Seeded blood group {}", name);
    }

    private void seedComponent(BloodComponentSeed seed) {
        if (bloodComponentsRepository.existsByBloodComponentName(seed.name())) {
            return;
        }
        BloodComponents component = new BloodComponents();
        component.setBloodComponentName(seed.name());
        component.setShelfLifeDays(seed.shelfLifeDays());
        component.setIsActive(true);
        bloodComponentsRepository.save(component);
        log.info("Seeded blood component {}", seed.name());
    }

    private record BloodComponentSeed(String name, Integer shelfLifeDays) {
    }
}
