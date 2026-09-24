package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.inventory.AddAvailabilityRequest;
import bloodbuddy.backend.dto.inventory.CentreInventoryResponse;
import bloodbuddy.backend.dto.inventory.InventoryAuditResponse;
import bloodbuddy.backend.dto.inventory.InventoryResponse;
import bloodbuddy.backend.dto.inventory.StockAdjustmentRequest;
import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.Inventory;
import bloodbuddy.backend.entity.InventoryAudit;
import bloodbuddy.backend.entity.masters.BloodComponents;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.enums.StockMovement;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.mapper.BloodCentreMapper;
import bloodbuddy.backend.mapper.InventoryMapper;
import bloodbuddy.backend.repository.BloodCentresRepository;
import bloodbuddy.backend.repository.BloodComponentsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import bloodbuddy.backend.repository.InventoryAuditRepository;
import bloodbuddy.backend.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAuditRepository inventoryAuditRepository;
    private final BloodCentresRepository bloodCentresRepository;
    private final BloodGroupRepository bloodGroupRepository;
    private final BloodComponentsRepository bloodComponentsRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryAuditRepository inventoryAuditRepository,
                            BloodCentresRepository bloodCentresRepository,
                            BloodGroupRepository bloodGroupRepository,
                            BloodComponentsRepository bloodComponentsRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryAuditRepository = inventoryAuditRepository;
        this.bloodCentresRepository = bloodCentresRepository;
        this.bloodGroupRepository = bloodGroupRepository;
        this.bloodComponentsRepository = bloodComponentsRepository;
    }

    /**
     * Add-availability upsert: one running row per (centre, group, component).
     * Increment when the row exists, otherwise create it. Always writes an ADD audit row.
     */
    @Transactional
    public CentreInventoryResponse addAvailability(Long bloodCentreId, AddAvailabilityRequest request, String actor) {
        BloodGroup bloodGroup = requireBloodGroup(request.getBloodGroupId());
        BloodComponents bloodComponent = requireBloodComponent(request.getBloodComponentId());

        Inventory inventory = inventoryRepository
                .findByBloodCentre_BloodCentreIdAndBloodGroup_BloodGroupIdAndBloodComponent_BloodComponentId(
                        bloodCentreId, request.getBloodGroupId(), request.getBloodComponentId())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (inventory == null) {
            BloodCentres centre = requireCentre(bloodCentreId);
            inventory = new Inventory();
            inventory.setBloodCentre(centre);
            inventory.setBloodGroup(bloodGroup);
            inventory.setBloodComponent(bloodComponent);
            inventory.setAvailableUnits(request.getUnits());
            inventory.setCreatedAt(now);
            inventory.setCreatedBy(actor);
        } else {
            inventory.setAvailableUnits(inventory.getAvailableUnits() + request.getUnits());
            inventory.setModifiedAt(now);
            inventory.setModifiedBy(actor);
        }
        inventory = inventoryRepository.save(inventory);

        writeAudit(inventory, StockMovement.ADD, request.getUnits(), request.getRemarks(), actor, now);
        return getCentreInventory(bloodCentreId);
    }

    /**
     * ISSUE / DISCARD / CORRECTION on an existing stock row. changedUnits is a signed
     * delta; the resulting available_units must never drop below zero.
     */
    @Transactional
    public CentreInventoryResponse adjustStock(Long bloodCentreId, StockAdjustmentRequest request, String actor) {
        if (request.getMovement() == StockMovement.ADD) {
            throw new BadRequestException("Use add-availability for ADD movements");
        }
        long delta = request.getChangedUnits();
        if (delta == 0) {
            throw new BadRequestException("changedUnits must be non-zero");
        }
        if ((request.getMovement() == StockMovement.ISSUE || request.getMovement() == StockMovement.DISCARD)
                && delta > 0) {
            throw new BadRequestException(request.getMovement() + " must reduce stock (changedUnits must be negative)");
        }

        Inventory inventory = inventoryRepository
                .findByBloodCentre_BloodCentreIdAndBloodGroup_BloodGroupIdAndBloodComponent_BloodComponentId(
                        bloodCentreId, request.getBloodGroupId(), request.getBloodComponentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock exists for the given group and component at this centre"));

        long newUnits = inventory.getAvailableUnits() + delta;
        if (newUnits < 0) {
            throw new BadRequestException("Insufficient stock: available units cannot go negative");
        }

        LocalDateTime now = LocalDateTime.now();
        inventory.setAvailableUnits(newUnits);
        inventory.setModifiedAt(now);
        inventory.setModifiedBy(actor);
        inventory = inventoryRepository.save(inventory);

        writeAudit(inventory, request.getMovement(), delta, request.getRemarks(), actor, now);
        return getCentreInventory(bloodCentreId);
    }

    /** Full centre details plus its stock (each item with group/component names). */
    @Transactional(readOnly = true)
    public CentreInventoryResponse getCentreInventory(Long bloodCentreId) {
        BloodCentres centre = requireCentre(bloodCentreId);
        List<InventoryResponse> inventory = inventoryRepository.findByBloodCentre_BloodCentreId(bloodCentreId).stream()
                .map(InventoryMapper::toResponse)
                .toList();
        return CentreInventoryResponse.builder()
                .bloodCentre(BloodCentreMapper.toResponse(centre))
                .inventory(inventory)
                .build();
    }

    /**
     * Stock ledger for a single inventory row, newest movement first. The row must belong to
     * the caller's centre, otherwise it is treated as not found.
     */
    @Transactional(readOnly = true)
    public List<InventoryAuditResponse> getInventoryHistory(Long bloodCentreId, Long inventoryId) {
        List<InventoryAudit> history = inventoryAuditRepository.findInventoryHistory(bloodCentreId, inventoryId);
        if (history.isEmpty()
                && !inventoryRepository.existsByInventoryIdAndBloodCentre_BloodCentreId(inventoryId, bloodCentreId)) {
            throw new ResourceNotFoundException("No inventory row found for id " + inventoryId + " at this centre");
        }
        return history.stream()
                .map(InventoryMapper::toAuditResponse)
                .toList();
    }

    private void writeAudit(Inventory inventory, StockMovement movement, long delta,
                            String remarks, String actor, LocalDateTime now) {
        InventoryAudit audit = new InventoryAudit();
        audit.setInventory(inventory);
        audit.setStockMovement(movement);
        audit.setChangedUnits(delta);
        audit.setRemainingUnits(inventory.getAvailableUnits());
        audit.setRemarks(remarks);
        audit.setCreatedAt(now);
        audit.setCreatedBy(actor);
        inventoryAuditRepository.save(audit);
    }

    private BloodGroup requireBloodGroup(Long id) {
        return bloodGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found: " + id));
    }

    private BloodComponents requireBloodComponent(Long id) {
        return bloodComponentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood component not found: " + id));
    }

    private BloodCentres requireCentre(Long id) {
        return bloodCentresRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood centre not found: " + id));
    }
}
