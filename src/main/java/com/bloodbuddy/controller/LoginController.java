package com.bloodbuddy.controller;

import com.bloodbuddy.dto.LoginRequest;
import com.bloodbuddy.dto.LoginResponse;
import com.bloodbuddy.services.LoginService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@AllArgsConstructor
public class LoginController {

    private final LoginService loginService;
    @PostMapping("/mail-password")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request, HttpSession session) {

        return ResponseEntity.ok(loginService.login(request,session));
    }

}
