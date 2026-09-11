package bloodbuddy.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Records a donor who agreed to and donated blood for a recipient's request. */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blood_request_donation")
public class BloodRequestDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_request_donation_id")
    private Long bloodRequestDonationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id")
    @JsonIgnore
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_donor_details_id")
    @JsonIgnore
    private BloodDonorDetails bloodDonorDetails;

    @Column(name = "donated_at")
    private LocalDateTime donatedAt;

    @Column(name = "created_by")
    private String createdBy;
}
