package com.helpdesk.helpdesk.ticket.dto;

import com.helpdesk.helpdesk.ticket.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateRequest(
        @NotNull
        TicketStatus status
) {}