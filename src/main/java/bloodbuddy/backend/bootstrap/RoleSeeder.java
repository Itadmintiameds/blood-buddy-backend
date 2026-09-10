package bloodbuddy.backend.bootstrap;

import bloodbuddy.backend.constants.RoleNames;
import bloodbuddy.backend.entity.masters.Roles;
import bloodbuddy.backend.repository.RolesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the roles the app depends on (SUPERADMIN, BLOOD_CENTRE) exist. Runs before
 * SuperAdminSeeder (@Order) so the admin seed can find its role. Idempotent: each role
 * is created only if absent, which also covers ddl-auto: create wiping the table on restart.
 */
@Component
@Order(1)
public class RoleSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleSeeder.class);

    private final RolesRepository rolesRepository;

    public RoleSeeder(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRole(RoleNames.SUPERADMIN);
        seedRole(RoleNames.BLOOD_CENTRE);
    }

    private void seedRole(String roleName) {
        if (rolesRepository.findByRoleName(roleName).isPresent()) {
            return;
        }
        Roles role = new Roles();
        role.setRoleName(roleName);
        role.setIsActive(true);
        rolesRepository.save(role);
        log.info("Seeded role {}", roleName);
    }
}
