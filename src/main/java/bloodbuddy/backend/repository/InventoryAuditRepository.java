package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {

    List<InventoryAudit> findByInventory_InventoryIdOrderByCreatedAtDesc(Long inventoryId);
}
