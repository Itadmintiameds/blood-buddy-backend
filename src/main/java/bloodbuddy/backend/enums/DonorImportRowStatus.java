package bloodbuddy.backend.enums;

/** Validation result of a staged import row. Only VALID rows are moved to the donor table on confirm. */
public enum DonorImportRowStatus {
    VALID,
    ERROR,
    /** A donor with the same mobile number already exists; always skipped. */
    DUPLICATE_IN_DB,
    /** An earlier (not excluded) row in the same file has the same mobile number; always skipped. */
    DUPLICATE_IN_FILE
}
