package bloodbuddy.backend.service;

/**
 * Outcome of an attempted notification send, persisted onto the {@code Notification} row by the
 * matching flow. {@code deliveryStatus} mirrors the stored value: {@code SENT}, {@code FAILED},
 * or {@code PENDING} (provider not configured, left for a later retry).
 */
public record NotificationResult(String deliveryStatus, String providerMessageId, String failureReason) {

    private static final int MAX_REASON_LENGTH = 1000;

    public static NotificationResult sent(String providerMessageId) {
        return new NotificationResult("SENT", providerMessageId, null);
    }

    public static NotificationResult failed(String failureReason) {
        return new NotificationResult("FAILED", null, truncate(failureReason));
    }

    /** Provider not configured: leave the row PENDING so it can be retried once wired in. */
    public static NotificationResult skipped() {
        return new NotificationResult("PENDING", null, null);
    }

    public boolean isSent() {
        return "SENT".equals(deliveryStatus);
    }

    private static String truncate(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() > MAX_REASON_LENGTH ? reason.substring(0, MAX_REASON_LENGTH) : reason;
    }
}
