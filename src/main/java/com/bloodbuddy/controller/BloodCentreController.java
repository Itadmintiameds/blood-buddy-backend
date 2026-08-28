package com.bloodbuddy.controller;

import com.bloodbuddy.dto.BloodCentreRegistrationRequest;
import com.bloodbuddy.services.BloodCentreService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/blood-centres")
@AllArgsConstructor
public class BloodCentreController {

    private final BloodCentreService bloodCentreService;

    @GetMapping("/bloodbuddy")
    public String bloodbuddyController() {
        return "Welcome to Bloodbuddy Centre";
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> register(
            @Valid @RequestBody BloodCentreRegistrationRequest request) {

        bloodCentreService.register(request);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "OTP sent successfully"
                )
        );
    }
}