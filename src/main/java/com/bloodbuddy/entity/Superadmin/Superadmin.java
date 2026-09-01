package com.bloodbuddy.entity.Superadmin;

import com.bloodbuddy.entity.BloodCentreReg;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "superadmin_blood_centre")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Superadmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_centre_name", nullable = false)
    @NotBlank(message = "Blood centre name is required")
    private String bloodCentreName;

    @Column(nullable = false)
    private String category;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must be 10 digits"
    )
    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(
            name = "blood_centre_id",
            referencedColumnName = "id"
    )
    private BloodCentreReg bloodCentre;
}