package bloodbuddy.backend.util;

import java.util.regex.Pattern;

/**
 * Canonical form for Indian donor mobile numbers: the bare 10-digit subscriber number
 * (e.g. {@code 9876543210}). Every write path (registration, superadmin edit, CSV import) stores this
 * form so the unique index on blood_donor_details.mobile_number catches duplicates however the
 * number was typed.
 */
public final class MobileNumbers {

    private static final Pattern CANONICAL = Pattern.compile("[6-9]\\d{9}");
    private static final Pattern ALLOWED_CHARS = Pattern.compile("[\\d\\s+()-]+");

    private MobileNumbers() {
    }

    /**
     * Strips spaces, dashes, brackets and a +91 / 0091 / 91 / 0 prefix. Returns null when the
     * result isn't a valid Indian mobile number (or the input has letters or other symbols).
     */
    public static String normalize(String raw) {
        if (raw == null || !ALLOWED_CHARS.matcher(raw.trim()).matches()) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 14 && digits.startsWith("0091")) {
            digits = digits.substring(4);
        } else if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return CANONICAL.matcher(digits).matches() ? digits : null;
    }
}
