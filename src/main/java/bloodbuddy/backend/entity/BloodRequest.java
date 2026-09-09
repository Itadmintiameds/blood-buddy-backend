package bloodbuddy.backend.entity;

import bloodbuddy.backend.entity.masters.BloodComponents;
import bloodbuddy.backend.entity.masters.BloodGroup;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blood_request")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_request_id")
    private Long bloodRequestId;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_component_id")
    @JsonIgnore
    private BloodComponents bloodComponent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_group_id")
    @JsonIgnore
    private BloodGroup bloodGroup;

    @Column(name = "required_units")
    private Long requiredUnits;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "address")
    private String address;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "modified_by")
    private String modifiedBy;
}
