package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.masters.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {

    boolean existsByBloodGroupName(String bloodGroupName);
}
