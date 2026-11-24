package bloodbuddy.backend.enums;

/**
 * Lifecycle of an uploaded donor CSV: staged for review, then confirmed. A discarded import is deleted,
 * so DISCARDED only appears in the discard endpoint's response.
 */
public enum DonorImportStatus {
    PENDING,
    CONFIRMED,
    DISCARDED
}
