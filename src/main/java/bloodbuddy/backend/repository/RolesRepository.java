package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.masters.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Long> {

    Optional<Roles> findByRoleName(String roleName);
}
