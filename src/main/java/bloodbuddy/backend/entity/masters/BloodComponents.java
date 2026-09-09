package bloodbuddy.backend.entity.masters;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blood_components")
public class BloodComponents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_component_id")
    private Long bloodComponentId;

    @Column(name = "blood_component_name", nullable = false, unique = true)
    private String bloodComponentName;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;
}
