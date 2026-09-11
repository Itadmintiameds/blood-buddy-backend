package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodRequestCentre;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestCentreRepository extends JpaRepository<BloodRequestCentre, Long> {

    // Eagerly fetch the centre so response mapping doesn't trigger N+1 lazy loads.
    @EntityGraph(attributePaths = "bloodCentre")
    List<BloodRequestCentre> findByBloodRequest_BloodRequestId(Long bloodRequestId);
}
