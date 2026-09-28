package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {

    List<InventoryAudit> findByInventory_InventoryIdOrderByCreatedAtDesc(Long inventoryId);

    // Stock ledger for a single inventory row, newest first. Scoped by centre as well so a
    // centre can only read the history of its own stock rows.
    @Query("SELECT a FROM InventoryAudit a "
            + "JOIN FETCH a.inventory i "
            + "JOIN FETCH i.bloodGroup g "
            + "JOIN FETCH i.bloodComponent c "
            + "WHERE i.inventoryId = :inventoryId "
            + "AND i.bloodCentre.bloodCentreId = :bloodCentreId "
            + "ORDER BY a.createdAt DESC")
    List<InventoryAudit> findInventoryHistory(@Param("bloodCentreId") Long bloodCentreId,
                                              @Param("inventoryId") Long inventoryId);
}
