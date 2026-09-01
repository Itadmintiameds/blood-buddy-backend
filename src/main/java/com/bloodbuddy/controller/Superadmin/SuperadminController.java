package com.bloodbuddy.controller.Superadmin;

import com.bloodbuddy.dto.Superadmin.SuperadminResponse;
import com.bloodbuddy.dto.Superadmin.SuperadminUpdateRequest;
import com.bloodbuddy.services.Superadmin.SuperadminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superadmin")
@RequiredArgsConstructor
public class SuperadminController {

    private final SuperadminService superadminService;


    @GetMapping("/blood_banks")
    public ResponseEntity<List<SuperadminResponse>> getSuperadmin() {

        return ResponseEntity.ok(superadminService.getAllSuperadmins());
    }
    @PutMapping("/edit/{id}")
    public ResponseEntity<SuperadminResponse> updateSuperadmin(
           @PathVariable Long id,
            @Valid @RequestBody SuperadminUpdateRequest request) {

        return ResponseEntity.ok(
                superadminService.updateSuperadmin(id, request)
        );
    }


}