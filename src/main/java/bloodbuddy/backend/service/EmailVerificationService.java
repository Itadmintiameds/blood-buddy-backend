package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.EmailVerification;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.repository.EmailVerificationRepository;
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

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_BOUND = 1_000_000; // 6-digit OTP: 000000-999999

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();
    private final String fromAddress;

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
                                    JavaMailSender mailSender,
                                    @Value("${spring.mail.username}") String fromAddress) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /** Issues a fresh OTP for the email (invalidating any prior one) and emails it. */
    @Transactional
    public void sendOtp(String email) {
        String otp = String.format("%06d", random.nextInt(OTP_BOUND));
        LocalDateTime now = LocalDateTime.now();

        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseGet(() -> {
                    EmailVerification created = new EmailVerification();
                    created.setEmail(email);
                    created.setCreatedAt(now);
                    return created;
                });
        verification.setOtp(otp);
        verification.setExpiryDate(now.plusMinutes(OTP_EXPIRY_MINUTES));
        verification.setVerified(false); // a new OTP clears any earlier verification
        verification.setModifiedAt(now);
        emailVerificationRepository.save(verification);

        sendOtpEmail(email, otp);
        log.info("OTP sent to {}", email);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No OTP was requested for this email"));

        if (verification.getExpiryDate() == null || verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired; please request a new one");
        }
        if (!verification.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        verification.setVerified(true);
        verification.setModifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }

    /** Registration guard: the email must have a verified record before a centre can be created. */
    @Transactional(readOnly = true)
    public void assertEmailVerified(String email) {
        boolean verified = emailVerificationRepository.findByEmail(email)
                .map(v -> Boolean.TRUE.equals(v.getVerified()))
                .orElse(false);
        if (!verified) {
            throw new BadRequestException("Email is not verified; verify the OTP sent to your email first");
        }
    }

    /** Consumes the verification after a successful registration so the OTP cannot be reused. */
    @Transactional
    public void clearVerification(String email) {
        emailVerificationRepository.findByEmail(email).ifPresent(emailVerificationRepository::delete);
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
}
