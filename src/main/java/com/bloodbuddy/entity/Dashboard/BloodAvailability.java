package com.bloodbuddy.entity.Dashboard;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tbl_blood_availability")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class BloodAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bloodGroup;

    @Column(nullable = false)
    private Integer units;
}
