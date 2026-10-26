package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.DonorStatus;
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

    // Candidate donors for admin outreach: same blood group, located in the recipient's
    // pincode/city/district, currently available, and eligible to donate. Available means not
    // deactivated and today is outside the lock window (a lock that has ended or not yet
    // started counts as available). Eligible means they have never donated or last donated on or before the
    // eligibility cut-off (today minus the minimum gap between donations). Ordered so the donors
    // who have gone longest without donating (never-donated first) come first, and the caller
    // caps the result with a Pageable limit.
    @Query("SELECT d FROM BloodDonorDetails d "
            + "WHERE d.bloodGroup.bloodGroupId = :bloodGroupId "
            + "AND (d.pincode = :pincode OR d.city = :city OR d.district = :district) "
            + "AND (d.status IS NULL OR d.status = bloodbuddy.backend.entity.DonorStatus.ACTIVE) "
            + "AND NOT (d.lockedUntil IS NOT NULL AND d.lockedUntil >= :today "
            + "         AND (d.lockedFrom IS NULL OR d.lockedFrom <= :today)) "
            + "AND (d.lastBloodDonationDate IS NULL OR d.lastBloodDonationDate <= :eligibleOnOrBefore) "
            + "ORDER BY d.lastBloodDonationDate ASC NULLS FIRST")
    List<BloodDonorDetails> findCandidateDonors(@Param("bloodGroupId") Long bloodGroupId,
                                                @Param("pincode") String pincode,
                                                @Param("city") String city,
                                                @Param("district") String district,
                                                @Param("today") LocalDate today,
                                                @Param("eligibleOnOrBefore") LocalDate eligibleOnOrBefore,
                                                Pageable pageable);

    @Query("SELECT DISTINCT d.city FROM BloodDonorDetails d WHERE d.city IS NOT NULL AND d.city <> '' ORDER BY d.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT d.district FROM BloodDonorDetails d WHERE d.district IS NOT NULL AND d.district <> '' ORDER BY d.district")
    List<String> findDistinctDistricts();

    @Query("SELECT COUNT(DISTINCT d.bloodGroup.bloodGroupId) FROM BloodDonorDetails d WHERE d.bloodGroup IS NOT NULL")
    long countDistinctBloodGroups();

    // Donors who last donated on or after the cut-off date (trailing window for the dashboard).
    long countByLastBloodDonationDateGreaterThanEqual(LocalDate sinceDate);

    long countByStatus(DonorStatus status);

    // Donors currently locked: not deactivated and the given date falls inside the lock window.
    @Query("SELECT COUNT(d) FROM BloodDonorDetails d "
            + "WHERE (d.status IS NULL OR d.status <> bloodbuddy.backend.entity.DonorStatus.DEACTIVATED) "
            + "AND d.lockedUntil IS NOT NULL AND d.lockedUntil >= :asOf "
            + "AND (d.lockedFrom IS NULL OR d.lockedFrom <= :asOf)")
    long countLockedAsOf(@Param("asOf") LocalDate asOf);
}
