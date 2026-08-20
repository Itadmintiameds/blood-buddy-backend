package com.bloodbuddy.controller;

import com.bloodbuddy.dto.ResetPasswordRequest;
import com.bloodbuddy.services.PasswordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@AllArgsConstructor
public class PasswordresetController {

    private final PasswordService passwordService;


    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(
                passwordService.resetPassword(request)
        );
    }
}