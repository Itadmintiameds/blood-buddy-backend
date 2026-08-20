package com.bloodbuddy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(
        name = "blood_centres_reg",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "sg_blood_centre_license_number",
                        columnNames = "bloodcetre_license_number"
                )
        }
)
@NoArgsConstructor
@Setter
@Getter
@ToString
public class BloodCentreReg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="blood_centre_name", nullable = false)
    @NotBlank(message = "Blood centre name is required")
    private String bloodCentreName;
    @NotBlank(message = "License number is required")
    @Column(name = "bloodcetre_license_number", nullable = false,unique = true)
    private String licenseNumber;
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    @Column(name="mobile_number")
    private String mobileNumber;
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;
    @NotBlank(message = "Password is required")
    @Column(name="password")
    private String password;
    @Column(name="confirm_password")
    private String confirmPassword;
    @Column(name = "address", nullable = false)
    private String address;
    @Column(name="pin_code")
    @NotBlank(message = "Pin code is required")

    @Pattern(regexp = "^[0-9]{6}$", message = "Pin code must be 6 digits")
    private String pincode;

}