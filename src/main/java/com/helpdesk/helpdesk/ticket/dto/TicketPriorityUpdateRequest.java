package com.helpdesk.helpdesk.ticket.dto;

import com.helpdesk.helpdesk.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record TicketPriorityUpdateRequest(
        @NotNull
        TicketPriority priority
) {}