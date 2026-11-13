package com.helpdesk.helpdesk.ticket.dto;

import com.helpdesk.helpdesk.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TicketCreateRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 10000)
        String description,

        @NotNull
        TicketPriority priority,

        @NotNull
        UUID categoryId

) {}