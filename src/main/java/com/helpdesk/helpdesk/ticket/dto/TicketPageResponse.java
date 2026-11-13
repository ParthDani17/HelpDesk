package com.helpdesk.helpdesk.ticket.dto;

import java.util.List;

public record TicketPageResponse(
        List<TicketResponse> tickets,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}