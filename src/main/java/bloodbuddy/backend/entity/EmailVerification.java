package bloodbuddy.backend.entity;

import bloodbuddy.backend.enums.VerificationPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks the latest OTP issued for an (email, purpose) pair and whether it has been verified.
 * The purpose keeps registration email verification and password reset OTPs on separate rows so
 * one can never satisfy the other — uniqueness is on (email, purpose), not email alone.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_verification",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email", "purpose"}))
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verification_id")
    private Long emailVerificationId;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private VerificationPurpose purpose;

    @Column(name = "otp")
    private String otp;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
