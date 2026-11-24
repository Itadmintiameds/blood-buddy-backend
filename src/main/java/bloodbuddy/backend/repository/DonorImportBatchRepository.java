package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.DonorImportBatch;
import bloodbuddy.backend.enums.DonorImportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface DonorImportBatchRepository extends JpaRepository<DonorImportBatch, Long> {

    @Override
    @EntityGraph(attributePaths = "consentForm")
    Page<DonorImportBatch> findAll(Pageable pageable);

    // The superadmin's unfinished import (at most one is allowed per user).
    Optional<DonorImportBatch> findFirstByUploadedByAndStatusOrderByUploadedAtDesc(String uploadedBy,
                                                                                  DonorImportStatus status);

    // Row lock so edits, confirm and discard on the same batch run one at a time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DonorImportBatch> findWithLockByDonorImportBatchId(Long donorImportBatchId);
}
