package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    // Eagerly fetch group + component so summary mapping doesn't trigger N+1 lazy loads.
    @Override
    @EntityGraph(attributePaths = {"bloodGroup", "bloodComponent"})
    List<BloodRequest> findAll();

    @Override
    @EntityGraph(attributePaths = {"bloodGroup", "bloodComponent"})
    Page<BloodRequest> findAll(Pageable pageable);
}
