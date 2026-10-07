package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodDonorDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BloodDonorDetailsRepository extends JpaRepository<BloodDonorDetails, Long>,
        JpaSpecificationExecutor<BloodDonorDetails> {

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

    @Query("SELECT DISTINCT d.city FROM BloodDonorDetails d WHERE d.city IS NOT NULL AND d.city <> '' ORDER BY d.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT d.district FROM BloodDonorDetails d WHERE d.district IS NOT NULL AND d.district <> '' ORDER BY d.district")
    List<String> findDistinctDistricts();

    @Query("SELECT COUNT(DISTINCT d.bloodGroup.bloodGroupId) FROM BloodDonorDetails d WHERE d.bloodGroup IS NOT NULL")
    long countDistinctBloodGroups();

    // Donors who last donated on or after the cut-off date (trailing window for the dashboard).
    long countByLastBloodDonationDateGreaterThanEqual(LocalDate sinceDate);
}
