package bloodbuddy.backend.enums;

/** Why an email OTP was issued — keeps registration verification and password reset separate. */
public enum VerificationPurpose {
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}
