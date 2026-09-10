package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Backing lookup for the add-availability upsert; the (centre, group, component)
    // trio is UNIQUE, so at most one row can match.
    Optional<Inventory> findByBloodCentre_BloodCentreIdAndBloodGroup_BloodGroupIdAndBloodComponent_BloodComponentId(
            Long bloodCentreId, Long bloodGroupId, Long bloodComponentId);

    List<Inventory> findByBloodCentre_BloodCentreId(Long bloodCentreId);

    // A centre matches a recipient when it stocks the requested group + component
    // with units on hand AND shares the recipient's pincode, city, or district.
    @Query("SELECT i FROM Inventory i JOIN FETCH i.bloodCentre c "
            + "WHERE i.bloodGroup.bloodGroupId = :bloodGroupId "
            + "AND i.bloodComponent.bloodComponentId = :bloodComponentId "
            + "AND i.availableUnits > 0 "
            + "AND (c.pincode = :pincode OR c.city = :city OR c.district = :district)")
    List<Inventory> findMatchingStock(@Param("bloodGroupId") Long bloodGroupId,
                                      @Param("bloodComponentId") Long bloodComponentId,
                                      @Param("pincode") String pincode,
                                      @Param("city") String city,
                                      @Param("district") String district);
}
