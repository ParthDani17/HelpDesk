package com.helpdesk.helpdesk.security.controller;

import com.helpdesk.helpdesk.security.service.JwtService;
import com.helpdesk.helpdesk.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test/jwt")
public class JwtTestController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtTestController(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate/{userId}")
    public ResponseEntity<String> generateToken(@PathVariable UUID userId) {

        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(jwtService.generateAccessToken(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        boolean valid = jwtService.isTokenValid(token);

        if (!valid) {
            return ResponseEntity.badRequest().body(
                    Map.of("valid", false)
            );
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "userId", jwtService.extractUserId(token),
                "role", jwtService.extractRole(token),
                "expiration", jwtService.extractExpiration(token).toString()
        ));
    }

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(
            Authentication authentication) {

        return ResponseEntity.ok(Map.of(
                "message", "You are authenticated!",
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }
}