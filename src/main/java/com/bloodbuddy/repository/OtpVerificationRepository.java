package com.bloodbuddy.repository;

import com.bloodbuddy.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByMobileNumberOrderByIdDesc(
            String mobileNumber
    );
}