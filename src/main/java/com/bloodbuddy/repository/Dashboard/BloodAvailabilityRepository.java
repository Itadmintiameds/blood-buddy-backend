package com.bloodbuddy.repository.Dashboard;

import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface BloodAvailabilityRepository extends JpaRepository<BloodAvailability, Integer> {

    boolean existsByBloodGroup(String bloodGroup);
}
