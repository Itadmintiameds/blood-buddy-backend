package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.EmailVerification;
import bloodbuddy.backend.enums.VerificationPurpose;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.repository.EmailVerificationRepository;
import bloodbuddy.backend.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Issues and verifies email OTPs for two distinct purposes — registration email verification and
 * password reset — kept on separate rows via {@link VerificationPurpose}. Because every lookup is
 * scoped by purpose, a password-reset OTP can never satisfy a registration check, or vice versa.
 */
@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_BOUND = 1_000_000; // 6-digit OTP: 000000-999999

    private final EmailVerificationRepository emailVerificationRepository;
    private final UsersRepository usersRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();
    private final String fromAddress;

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
                                    UsersRepository usersRepository,
                                    JavaMailSender mailSender,
                                    @Value("${spring.mail.username}") String fromAddress) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.usersRepository = usersRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    // ---------------------------------------------------------------------------------------------
    // Registration email verification (purpose = EMAIL_VERIFICATION)
    // ---------------------------------------------------------------------------------------------

    /** Issues a fresh registration OTP for the email (invalidating any prior one) and emails it. */
    @Transactional
    public void sendOtp(String email) {
        // No point verifying an email that already has an account (username == email).
        if (usersRepository.existsByEmail(email) || usersRepository.existsByUsername(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        String otp = issueOtp(email, VerificationPurpose.EMAIL_VERIFICATION);
        sendOtpEmail(email, otp);
        log.info("Verification OTP sent to {}", email);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        EmailVerification verification = validateOtp(email, otp, VerificationPurpose.EMAIL_VERIFICATION);
        verification.setVerified(true);
        verification.setModifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }

    /** Registration guard: the email must have a verified record before a centre can be created. */
    @Transactional(readOnly = true)
    public void assertEmailVerified(String email) {
        boolean verified = emailVerificationRepository
                .findByEmailAndPurpose(email, VerificationPurpose.EMAIL_VERIFICATION)
                .map(v -> Boolean.TRUE.equals(v.getVerified()))
                .orElse(false);
        if (!verified) {
            throw new BadRequestException("Email is not verified; verify the OTP sent to your email first");
        }
    }

    /** Consumes the verification after a successful registration so the OTP cannot be reused. */
    @Transactional
    public void clearVerification(String email) {
        emailVerificationRepository.findByEmailAndPurpose(email, VerificationPurpose.EMAIL_VERIFICATION)
                .ifPresent(emailVerificationRepository::delete);
    }

    // ---------------------------------------------------------------------------------------------
    // Password reset (purpose = PASSWORD_RESET)
    // ---------------------------------------------------------------------------------------------

    /**
     * Issues a password-reset OTP, but only for an email that actually has an account. To avoid
     * leaking which emails are registered, this stays silent (no exception, no email) when the
     * account does not exist — callers respond the same way regardless.
     */
    @Transactional
    public void sendPasswordResetOtp(String email) {
        if (!usersRepository.existsByEmail(email)) {
            log.info("Password reset requested for unknown email {}; ignoring", email);
            return;
        }

        String otp = issueOtp(email, VerificationPurpose.PASSWORD_RESET);
        sendPasswordResetEmail(email, otp);
        log.info("Password reset OTP sent to {}", email);
    }

    /** Validates a password-reset OTP, throwing if it is missing, expired, or wrong. */
    @Transactional(readOnly = true)
    public void verifyPasswordResetOtp(String email, String otp) {
        validateOtp(email, otp, VerificationPurpose.PASSWORD_RESET);
    }

    /** Consumes the password-reset OTP after a successful reset so it cannot be reused. */
    @Transactional
    public void clearPasswordReset(String email) {
        emailVerificationRepository.findByEmailAndPurpose(email, VerificationPurpose.PASSWORD_RESET)
                .ifPresent(emailVerificationRepository::delete);
    }

    // ---------------------------------------------------------------------------------------------
    // Shared OTP mechanics
    // ---------------------------------------------------------------------------------------------

    /** Generates, upserts (per email+purpose), and returns a fresh OTP, clearing any prior state. */
    private String issueOtp(String email, VerificationPurpose purpose) {
        String otp = String.format("%06d", random.nextInt(OTP_BOUND));
        LocalDateTime now = LocalDateTime.now();

        EmailVerification verification = emailVerificationRepository.findByEmailAndPurpose(email, purpose)
                .orElseGet(() -> {
                    EmailVerification created = new EmailVerification();
                    created.setEmail(email);
                    created.setPurpose(purpose);
                    created.setCreatedAt(now);
                    return created;
                });
        verification.setOtp(otp);
        verification.setExpiryDate(now.plusMinutes(OTP_EXPIRY_MINUTES));
        verification.setVerified(false); // a new OTP clears any earlier verification
        verification.setModifiedAt(now);
        emailVerificationRepository.save(verification);
        return otp;
    }

    /** Looks up and validates an OTP for the given purpose; returns the row on success. */
    private EmailVerification validateOtp(String email, String otp, VerificationPurpose purpose) {
        EmailVerification verification = emailVerificationRepository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new BadRequestException("No OTP was requested for this email"));

        if (verification.getExpiryDate() == null || verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired; please request a new one");
        }
        if (!verification.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }
        return verification;
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("BloodBuddy email verification");
            message.setText("Your BloodBuddy verification OTP is " + otp
                    + ". It is valid for " + OTP_EXPIRY_MINUTES + " minutes.");
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send OTP email to {}", email, ex);
            throw new BadRequestException("Could not send OTP email; please try again later");
        }
    }

    private void sendPasswordResetEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("BloodBuddy password reset");
            message.setText("Your BloodBuddy password reset OTP is " + otp
                    + ". It is valid for " + OTP_EXPIRY_MINUTES + " minutes."
                    + " If you did not request a password reset, you can ignore this email.");
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send password reset email to {}", email, ex);
            throw new BadRequestException("Could not send password reset email; please try again later");
        }
    }
}
