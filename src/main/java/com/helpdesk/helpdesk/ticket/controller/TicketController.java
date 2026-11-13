package com.helpdesk.helpdesk.ticket.controller;

import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import com.helpdesk.helpdesk.ticket.dto.TicketCreateRequest;
import com.helpdesk.helpdesk.ticket.dto.TicketResponse;
import com.helpdesk.helpdesk.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.helpdesk.helpdesk.ticket.dto.TicketAssignmentRequest;
import com.helpdesk.helpdesk.ticket.dto.TicketStatusUpdateRequest;
import com.helpdesk.helpdesk.ticket.dto.TicketPriorityUpdateRequest;
import com.helpdesk.helpdesk.ticket.dto.TicketCategoryUpdateRequest;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.createTicket(
                request,
                userDetails.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("@ticketAuthorizationService.canAccessTicket(#ticketId, authentication)")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable UUID ticketId
    ) {
        TicketResponse response = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'ADMIN')")
    public ResponseEntity<List<TicketResponse>> getTickets(
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        List<TicketResponse> response = ticketService.getTickets(userDetails);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketAssignmentRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.assignTicket(
                ticketId,
                request.agentId(),
                userDetails.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("""
    hasRole('ADMIN') or
    (hasRole('AGENT') and @ticketAuthorizationService.isAssignee(#ticketId, authentication.principal.id))
    """)
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.updateTicketStatus(
                ticketId,
                request.status(),
                userDetails.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/priority")
    @PreAuthorize("""
    hasRole('ADMIN') or
    (hasRole('AGENT') and @ticketAuthorizationService.isAssignee(#ticketId, authentication.principal.id))
    """)
    public ResponseEntity<TicketResponse> updateTicketPriority(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketPriorityUpdateRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.updateTicketPriority(
                ticketId,
                request.priority(),
                userDetails.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/category")
    @PreAuthorize("""
    hasRole('ADMIN') or
    (hasRole('AGENT') and @ticketAuthorizationService.isAssignee(#ticketId, authentication.principal.id))
    """)
    public ResponseEntity<TicketResponse> updateTicketCategory(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketCategoryUpdateRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.updateTicketCategory(
                ticketId,
                request.categoryId(),
                userDetails.getId()
        );

        return ResponseEntity.ok(response);
    }
}