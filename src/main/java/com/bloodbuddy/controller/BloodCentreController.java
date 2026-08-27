package com.bloodbuddy.controller;

import com.bloodbuddy.dto.BloodCentreRegistrationRequest;

import com.bloodbuddy.entity.BloodCentreReg;
import com.bloodbuddy.services.BloodCentreService;
import com.bloodbuddy.services.TwilioOtpService;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/blood-centres")
@AllArgsConstructor
public class BloodCentreController {

    private final BloodCentreService bloodCentreService;


    @GetMapping("/bloodbuddy")
    public String BloodbuddyController() {
        return "Welcome to Bloodbuddy Centre";
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> register(
            @Valid @RequestBody
            BloodCentreRegistrationRequest request) {

        try {

            BloodCentreReg bloodCentre =
                    bloodCentreService.register(request);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "OTP sent successfully"
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getMessage()
                            )
                    );
        }
    }
}