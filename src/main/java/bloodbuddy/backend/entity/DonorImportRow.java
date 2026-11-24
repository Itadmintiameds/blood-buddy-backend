package bloodbuddy.backend.entity;

import bloodbuddy.backend.enums.DonorImportRowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A staged CSV row awaiting review. Values are kept as text exactly as uploaded (or as edited by the
 * superadmin) so invalid input can be shown and corrected; DonorImportService parses and validates
 * them into {@link #status} / {@link #errors}. Rows only live while their batch is PENDING: confirm
 * and discard both delete them.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "donor_import_row", indexes = {
        @Index(name = "idx_import_row_batch", columnList = "donor_import_batch_id, row_no")
})
public class DonorImportRow {

    // Sequence (not identity) so the thousands of rows of an upload can be inserted in JDBC batches.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "donor_import_row_seq")
    @SequenceGenerator(name = "donor_import_row_seq", sequenceName = "donor_import_row_seq", allocationSize = 50)
    @Column(name = "donor_import_row_id")
    private Long donorImportRowId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_import_batch_id", nullable = false)
    private DonorImportBatch batch;

    // Spreadsheet row number (the header is row 1).
    @Column(name = "row_no", nullable = false)
    private Integer rowNo;

    @Column(name = "full_name", length = 1000)
    private String fullName;

    @Column(name = "mobile_number", length = 1000)
    private String mobileNumber;

    @Column(name = "alternative_mobile_number", length = 1000)
    private String alternativeMobileNumber;

    @Column(name = "blood_group", length = 1000)
    private String bloodGroup;

    @Column(name = "dob", length = 1000)
    private String dob;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "city", length = 1000)
    private String city;

    @Column(name = "district", length = 1000)
    private String district;

    @Column(name = "pincode", length = 1000)
    private String pincode;

    @Column(name = "last_blood_donation_date", length = 1000)
    private String lastBloodDonationDate;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DonorImportRowStatus status;

    // Validation messages, one per line.
    @Column(name = "errors", columnDefinition = "TEXT")
    private String errors;

    // DUPLICATE_IN_DB: the existing donor with the same mobile number.
    @Column(name = "existing_donor_id")
    private Long existingDonorId;

    // DUPLICATE_IN_FILE: the earlier row with the same mobile number.
    @Column(name = "duplicate_of_row")
    private Integer duplicateOfRow;

    // Unticked by the superadmin during review; never imported.
    @Column(name = "excluded", nullable = false)
    private boolean excluded;
}
