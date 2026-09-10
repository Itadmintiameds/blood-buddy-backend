package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodRequestDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestDetailsRepository extends JpaRepository<BloodRequestDetails, Long> {

    List<BloodRequestDetails> findByBloodRequest_BloodRequestId(Long bloodRequestId);
}
