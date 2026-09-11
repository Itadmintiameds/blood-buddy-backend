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

/** A blood centre surfaced to the recipient's request at submission (location + matching stock). */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blood_request_centre")
public class BloodRequestCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_request_centre_id")
    private Long bloodRequestCentreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_request_id")
    @JsonIgnore
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_centre_id")
    @JsonIgnore
    private BloodCentres bloodCentre;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Column(name = "created_by")
    private String createdBy;
}
