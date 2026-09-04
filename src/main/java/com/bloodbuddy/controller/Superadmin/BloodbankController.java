package com.bloodbuddy.controller.Superadmin;

import com.bloodbuddy.dto.Superadmin.*;
import com.bloodbuddy.services.Superadmin.BloodbankService;
import com.bloodbuddy.services.Superadmin.SuperAdminUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superadmin")
@RequiredArgsConstructor
public class BloodbankController {

    private final BloodbankService bloodbankService;
    private final SuperAdminUserService superAdminUserService;

    @PostMapping("/adduser")
    public ResponseEntity<?> register(
            @Valid @RequestBody SuperAdminLoginRequest request, HttpSession session) {

        String message = superAdminUserService.register(request,session);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", message));
    }

    @PostMapping("/login")
    public ResponseEntity<SuperAdminLoginResponse> login(
            @Valid @RequestBody SuperAdminUserRequest request, HttpSession session) {


        return ResponseEntity.ok(superAdminUserService.login(request,session));
    }

    @GetMapping("/blood_banks")
    public ResponseEntity<List<SuperadminResponse>> getSuperadmin() {

        return ResponseEntity.ok(bloodbankService.getAllSuperadmins());
    }
    @PutMapping("/edit/{id}")
    public ResponseEntity<SuperadminResponse> updateSuperadmin(
           @PathVariable Long id,
            @Valid @RequestBody SuperadminUpdateRequest request) {

        return ResponseEntity.ok(
                bloodbankService.updateSuperadmin(id, request)
        );
    }


}