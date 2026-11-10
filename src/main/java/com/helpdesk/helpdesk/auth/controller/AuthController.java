package com.helpdesk.helpdesk.auth.controller;

import com.helpdesk.helpdesk.auth.dto.LoginRequest;
import com.helpdesk.helpdesk.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.helpdesk.helpdesk.auth.dto.RegisterRequest;
import com.helpdesk.helpdesk.auth.dto.RegisterResponse;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication =
                authService.authenticate(
                        loginRequest.email(),
                        loginRequest.password()
                );

        return ResponseEntity.ok(
                "Authentication successful for: " + authentication.getName()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        RegisterResponse response = authService.register(registerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}