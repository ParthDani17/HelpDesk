package com.helpdesk.helpdesk.ticket.dto;

import com.helpdesk.helpdesk.history.enums.TicketHistoryAction;

import java.time.Instant;
import java.util.UUID;

public record TicketHistoryResponse(
        UUID id,
        UUID ticketId,
        UUID performedById,
        String performedByName,
        TicketHistoryAction action,
        String oldValue,
        String newValue,
        Instant createdAt
) {}