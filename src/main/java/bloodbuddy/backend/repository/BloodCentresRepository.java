package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.BloodCentres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BloodCentresRepository extends JpaRepository<BloodCentres, Long>,
        JpaSpecificationExecutor<BloodCentres> {

    boolean existsByEmail(String email);

    long countByIsActiveTrue();

    // Active centres needing attention: at least one inventory item at/below the threshold
    // (<= threshold includes 0/out-of-stock) OR no inventory rows at all.
    @Query("SELECT COUNT(c) FROM BloodCentres c WHERE c.isActive = true AND ("
            + "EXISTS (SELECT i FROM Inventory i WHERE i.bloodCentre = c AND i.availableUnits <= :threshold) "
            + "OR NOT EXISTS (SELECT i2 FROM Inventory i2 WHERE i2.bloodCentre = c))")
    long countLowStockActiveCentres(@Param("threshold") long threshold);

    @Query("SELECT DISTINCT c.city FROM BloodCentres c WHERE c.city IS NOT NULL AND c.city <> '' ORDER BY c.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT c.district FROM BloodCentres c WHERE c.district IS NOT NULL AND c.district <> '' ORDER BY c.district")
    List<String> findDistinctDistricts();
}
