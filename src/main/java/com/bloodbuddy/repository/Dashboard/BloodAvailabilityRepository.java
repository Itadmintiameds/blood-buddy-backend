package com.bloodbuddy.repository.Dashboard;

import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BloodAvailabilityRepository extends JpaRepository<BloodAvailability, Long> {

    Optional<BloodAvailability> findByBloodCentreIdAndBloodGroupAndBloodType(Long bloodCentreId,String bloodGroup, String bloodType);

    boolean existsByBloodGroup(String bloodGroup);
}