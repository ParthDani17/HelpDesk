package com.helpdesk.helpdesk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketReopenRequest(
        @NotBlank
        @Size(max = 1000)
        String reason
) {}