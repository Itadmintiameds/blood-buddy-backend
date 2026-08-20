package com.bloodbuddy.repository;

import com.bloodbuddy.entity.BloodCentreReg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface BloodCentreRepository  extends JpaRepository<BloodCentreReg, Long> {

    boolean existsByLicenseNumber(String bloodCentrelicenseNumber);

    Optional<BloodCentreReg> findByLicenseNumber(String bloodCentrelicenseNumber);

    Optional<BloodCentreReg> findByEmail(String email);
}