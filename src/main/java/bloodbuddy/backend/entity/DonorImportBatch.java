package bloodbuddy.backend.entity;

import bloodbuddy.backend.enums.DonorImportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One superadmin CSV upload of donors. Its rows are staged in {@link DonorImportRow} for review and
 * only copied into blood_donor_details when the batch is confirmed. The review counts are refreshed
 * every time the rows are re-validated; inserted/skipped are set on confirm.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "donor_import_batch")
public class DonorImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donor_import_batch_id")
    private Long donorImportBatchId;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DonorImportStatus status;

    @Column(name = "total_rows")
    private Integer totalRows;

    // Not excluded and VALID: what confirm would import right now.
    @Column(name = "ready_rows")
    private Integer readyRows;

    @Column(name = "error_rows")
    private Integer errorRows;

    @Column(name = "duplicate_rows")
    private Integer duplicateRows;

    @Column(name = "excluded_rows")
    private Integer excludedRows;

    @Column(name = "inserted_rows")
    private Integer insertedRows;

    @Column(name = "skipped_rows")
    private Integer skippedRows;

    // Consent version the superadmin attested the imported donors agreed to offline.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_form_id")
    private ConsentForm consentForm;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "confirmed_by")
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
}
