package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodDonorDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BloodDonorDetailsRepository extends JpaRepository<BloodDonorDetails, Long> {

    // Eagerly fetch bloodGroup so DonorResponse mapping doesn't trigger N+1 lazy loads.
    @Override
    @EntityGraph(attributePaths = "bloodGroup")
    List<BloodDonorDetails> findAll();

    @Override
    @EntityGraph(attributePaths = "bloodGroup")
    Page<BloodDonorDetails> findAll(Pageable pageable);

    // Candidate donors for admin outreach: same blood group and located in the
    // recipient's pincode, city, or district.
    @Query("SELECT d FROM BloodDonorDetails d "
            + "WHERE d.bloodGroup.bloodGroupId = :bloodGroupId "
            + "AND (d.pincode = :pincode OR d.city = :city OR d.district = :district)")
    List<BloodDonorDetails> findCandidateDonors(@Param("bloodGroupId") Long bloodGroupId,
                                                @Param("pincode") String pincode,
                                                @Param("city") String city,
                                                @Param("district") String district);
}
