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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// One running stock row per (centre, group, component); units may never go negative.
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(
        name = "uq_inventory_centre_group_component",
        columnNames = {"blood_centre_id", "blood_group_id", "blood_component_id"}))
@Check(name = "chk_inventory_units_non_negative", constraints = "available_units >= 0")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_centre_id")
    @JsonIgnore
    private BloodCentres bloodCentre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_group_id")
    @JsonIgnore
    private BloodGroup bloodGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_component_id")
    @JsonIgnore
    private BloodComponents bloodComponent;

    @Column(name = "available_units")
    private Long availableUnits;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "modified_by")
    private String modifiedBy;
}
