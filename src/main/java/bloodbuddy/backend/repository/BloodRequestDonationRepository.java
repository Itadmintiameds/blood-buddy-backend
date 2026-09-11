package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodRequestDonation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestDonationRepository extends JpaRepository<BloodRequestDonation, Long> {

    // Eagerly fetch the donor (and its blood group) so response mapping doesn't trigger N+1 lazy loads.
    @EntityGraph(attributePaths = {"bloodDonorDetails", "bloodDonorDetails.bloodGroup"})
    List<BloodRequestDonation> findByBloodRequest_BloodRequestId(Long bloodRequestId);
}
