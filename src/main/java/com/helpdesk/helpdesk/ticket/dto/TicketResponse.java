package com.helpdesk.helpdesk.ticket.dto;

import com.helpdesk.helpdesk.ticket.enums.TicketPriority;
import com.helpdesk.helpdesk.ticket.enums.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(

        UUID id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        UUID creatorId,
        UUID assigneeId,
        UUID categoryId,
        Long version,
        Instant createdAt,
        Instant updatedAt

) {}