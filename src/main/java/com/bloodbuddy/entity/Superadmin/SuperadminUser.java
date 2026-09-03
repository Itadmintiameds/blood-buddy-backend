package com.bloodbuddy.entity.Superadmin;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "superadmin_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SuperadminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


}