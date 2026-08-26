package com.bloodbuddy.entity.Dashboard;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tbl_blood_availability")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class BloodAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="blood_group", nullable = false)
    private String bloodGroup;

    @Column(name="blood_type", nullable = false)
    private String bloodType;

    @Column(name="units_available", nullable = false)
    private Integer unitsAvailable;



}
