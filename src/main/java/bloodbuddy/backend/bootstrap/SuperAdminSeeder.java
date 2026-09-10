package bloodbuddy.backend.bootstrap;

import bloodbuddy.backend.constants.RoleNames;
import bloodbuddy.backend.entity.Users;
import bloodbuddy.backend.entity.masters.Roles;
import bloodbuddy.backend.repository.RolesRepository;
import bloodbuddy.backend.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seeds a single SUPERADMIN account on startup from SUPERADMIN_EMAIL / SUPERADMIN_PASSWORD.
 * Idempotent: skips if the account already exists. Never creates roles -- the SUPERADMIN role
 * must already exist, otherwise startup fails fast so the operator seeds master data first.
 */
@Component
public class SuperAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public SuperAdminSeeder(UsersRepository usersRepository,
                            RolesRepository rolesRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${superadmin.email}") String email,
                            @Value("${superadmin.password}") String password) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usersRepository.existsByUsername(email) || usersRepository.existsByEmail(email)) {
            log.info("SUPERADMIN account already present for {}; skipping seed", email);
            return;
        }

        Roles superAdminRole = rolesRepository.findByRoleName(RoleNames.SUPERADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Required role '" + RoleNames.SUPERADMIN + "' is missing from the roles table. "
                                + "Seed the master roles before starting the application."));

        Users user = new Users();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(superAdminRole);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("SYSTEM");
        usersRepository.save(user);

        log.info("Seeded SUPERADMIN account for {}", email);
    }
}
