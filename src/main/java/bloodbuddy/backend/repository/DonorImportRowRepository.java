package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.DonorImportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorImportRowRepository extends JpaRepository<DonorImportRow, Long>,
        JpaSpecificationExecutor<DonorImportRow> {

    List<DonorImportRow> findByBatchDonorImportBatchIdOrderByRowNo(Long batchId);

    Optional<DonorImportRow> findByDonorImportRowIdAndBatchDonorImportBatchId(Long rowId, Long batchId);

    // Drops a discarded batch's staged rows in one statement.
    @Modifying
    @Query("DELETE FROM DonorImportRow r WHERE r.batch.donorImportBatchId = :batchId")
    int deleteByBatchId(@Param("batchId") Long batchId);
}
