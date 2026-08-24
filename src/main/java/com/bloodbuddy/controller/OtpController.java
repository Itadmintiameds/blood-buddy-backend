package com.bloodbuddy.controller;

import com.bloodbuddy.dto.OtpRequest;
import com.bloodbuddy.dto.OtpVerifyRequest;
import com.bloodbuddy.services.TwilioOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpController {

    private final TwilioOtpService twilioOtpService;

    public OtpController(TwilioOtpService twilioOtpService) {
        this.twilioOtpService = twilioOtpService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(
            @Valid @RequestBody OtpRequest request) {

        try {

            String status = twilioOtpService.sendOtp(
                    request.mobileNumber()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "OTP sent successfully",
                            "status", status
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(500).body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {

        try {

            boolean verified = twilioOtpService.verifyOtp(
                    request.mobileNumber(),
                    request.otp()
            );

            if (verified) {

                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "message", "OTP verified successfully"
                        )
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Invalid or expired OTP"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(500).body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        }
    }
}