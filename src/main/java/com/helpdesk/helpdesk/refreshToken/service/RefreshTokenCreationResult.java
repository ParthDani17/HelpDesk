package com.helpdesk.helpdesk.refreshToken.service;

import java.util.UUID;

public record RefreshTokenCreationResult(
        String refreshToken,
        UUID sessionId
) {}