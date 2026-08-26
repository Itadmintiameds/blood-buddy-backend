package com.bloodbuddy.repository.Dashboard;

import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BloodAvailabilityRepository
        extends JpaRepository<BloodAvailability, Long> {

    Optional<BloodAvailability> findByBloodGroup(String bloodGroup);

    boolean existsByBloodGroup(String bloodGroup);
}