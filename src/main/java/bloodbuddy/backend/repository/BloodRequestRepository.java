package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.BloodRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long>,
        JpaSpecificationExecutor<BloodRequest> {

    // Eagerly fetch group + component so summary mapping doesn't trigger N+1 lazy loads.
    @Override
    @EntityGraph(attributePaths = {"bloodGroup", "bloodComponent"})
    List<BloodRequest> findAll();

    @Override
    @EntityGraph(attributePaths = {"bloodGroup", "bloodComponent"})
    Page<BloodRequest> findAll(Pageable pageable);

    @Query("SELECT DISTINCT r.city FROM BloodRequest r WHERE r.city IS NOT NULL AND r.city <> '' ORDER BY r.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT r.district FROM BloodRequest r WHERE r.district IS NOT NULL AND r.district <> '' ORDER BY r.district")
    List<String> findDistinctDistricts();

    long countByStatus(BloodRequestStatus status);

    long countByStatusIn(Collection<BloodRequestStatus> statuses);
}
