package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.masters.BloodComponents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodComponentsRepository extends JpaRepository<BloodComponents, Long> {
}
