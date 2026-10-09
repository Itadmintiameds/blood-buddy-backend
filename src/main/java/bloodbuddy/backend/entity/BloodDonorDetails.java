package bloodbuddy.backend.entity;

import bloodbuddy.backend.entity.masters.BloodGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blood_donor_details", indexes = {
        @Index(name = "idx_donor_blood_group", columnList = "blood_group_id"),
        @Index(name = "idx_donor_city", columnList = "city"),
        @Index(name = "idx_donor_district", columnList = "district"),
        @Index(name = "idx_donor_pincode", columnList = "pincode"),
        @Index(name = "idx_donor_last_donation", columnList = "last_blood_donation_date"),
        @Index(name = "idx_donor_status", columnList = "status")
})
public class BloodDonorDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_donor_details_id")
    private Long bloodDonorDetailsId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "alternative_mobile_number")
    private String alternativeMobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_group_id")
    @JsonIgnore
    private BloodGroup bloodGroup;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "address")
    private String address;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "last_blood_donation_date")
    private LocalDate lastBloodDonationDate;

    // Availability lifecycle. Null is treated as ACTIVE everywhere (existing rows are not
    // backfilled by ddl-auto: update).
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DonorStatus status;

    // Reason captured when the donor is locked or deactivated.
    @Enumerated(EnumType.STRING)
    @Column(name = "unavailability_reason")
    private DonorUnavailabilityReason unavailabilityReason;

    @Column(name = "remarks")
    private String remarks;

    // Lock window; the donor auto-reappears once lockedUntil has passed.
    @Column(name = "locked_from")
    private LocalDate lockedFrom;

    @Column(name = "locked_until")
    private LocalDate lockedUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "modified_by")
    private String modifiedBy;
}
