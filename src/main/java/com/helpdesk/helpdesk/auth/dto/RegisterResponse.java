package com.helpdesk.helpdesk.auth.dto;

import com.helpdesk.helpdesk.user.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(

        UUID id,
        String name,
        String email,
        UserRole role,
        Instant createdAt

) {
}