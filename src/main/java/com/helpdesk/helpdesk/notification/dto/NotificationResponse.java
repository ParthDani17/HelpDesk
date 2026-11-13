package com.helpdesk.helpdesk.notification.dto;

import com.helpdesk.helpdesk.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID ticketId,
        String message,
        NotificationType type,
        boolean read,
        Instant createdAt
) {}