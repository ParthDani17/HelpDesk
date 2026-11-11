package com.helpdesk.helpdesk.auth.controller;

import com.helpdesk.helpdesk.auth.dto.LoginRequest;
import com.helpdesk.helpdesk.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.helpdesk.helpdesk.auth.dto.RegisterRequest;
import com.helpdesk.helpdesk.auth.dto.RegisterResponse;
import org.springframework.http.HttpStatus;
import com.helpdesk.helpdesk.auth.dto.LoginResponse;
import com.helpdesk.helpdesk.auth.dto.RefreshRequest;
import com.helpdesk.helpdesk.auth.dto.RefreshResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse response = authService.login(
                loginRequest.email(),
                loginRequest.password()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        RegisterResponse response = authService.register(registerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest refreshRequest) {

        RefreshResponse response =
                authService.refresh(refreshRequest.refreshToken());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshRequest refreshRequest) {

        authService.logout(refreshRequest.refreshToken());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutFromAllDevices(
            @Valid @RequestBody RefreshRequest refreshRequest) {

        authService.logoutFromAllDevices(refreshRequest.refreshToken());

        return ResponseEntity.noContent().build();
    }


}