package com.helpdesk.helpdesk.ticket.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TicketAssignmentRequest(
        @NotNull
        UUID agentId
) {}