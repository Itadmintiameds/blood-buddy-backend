package com.bloodbuddy.repository.superadmin;

import com.bloodbuddy.entity.Superadmin.SuperadminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperadminLoginRepository extends JpaRepository<SuperadminUser, Long> {
    Optional<SuperadminUser> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}