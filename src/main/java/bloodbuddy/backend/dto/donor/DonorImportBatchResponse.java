package bloodbuddy.backend.dto.donor;

import bloodbuddy.backend.enums.DonorImportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Summary of a donor import batch. Inserted/skipped counts are set once confirmed. */
@Getter
@Builder
public class DonorImportBatchResponse {

    private final Long batchId;
    private final String fileName;
    private final DonorImportStatus status;

    private final Integer totalRows;
    /** Rows that confirm would import right now (VALID and not excluded). */
    private final Integer readyRows;
    private final Integer errorRows;
    private final Integer duplicateRows;
    private final Integer excludedRows;

    private final Integer insertedRows;
    private final Integer skippedRows;
    private final String consentVersion;

    private final String uploadedBy;
    private final LocalDateTime uploadedAt;
    private final String confirmedBy;
    private final LocalDateTime confirmedAt;
}
