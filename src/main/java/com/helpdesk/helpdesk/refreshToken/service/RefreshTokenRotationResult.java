package com.helpdesk.helpdesk.refreshToken.service;

import com.helpdesk.helpdesk.user.enums.UserRole;

import java.util.UUID;

public record RefreshTokenRotationResult(
        String refreshToken,
        UUID userId,
        UserRole role
) {
}