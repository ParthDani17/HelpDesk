package com.helpdesk.helpdesk.auth.dto;

import com.helpdesk.helpdesk.user.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String name,
        String email,
        UserRole role,
        Instant createdAt
) {
}