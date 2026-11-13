package com.helpdesk.helpdesk.ticket.controller;

import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import com.helpdesk.helpdesk.ticket.dto.*;
import com.helpdesk.helpdesk.ticket.enums.TicketPriority;
import com.helpdesk.helpdesk.ticket.enums.TicketStatus;
import com.helpdesk.helpdesk.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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
    public ResponseEntity<TicketPageResponse> getTickets(
            @AuthenticationPrincipal HelpDeskUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate
    ) {
        TicketPageResponse response = ticketService.getTickets(
                userDetails,
                page,
                size,
                sortBy,
                direction,
                status,
                priority,
                categoryId,
                keyword,
                startDate,
                endDate
        );

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

    @PatchMapping("/{ticketId}/reopen")
    @PreAuthorize("""
        hasRole('ADMIN') or
        (hasRole('USER') and @ticketService.isTicketCreator(#ticketId, authentication.principal.id))
        """)
    public ResponseEntity<TicketResponse> reopenTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketReopenRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.reopenTicket(
                ticketId,
                request.reason(),
                userDetails.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> closeTicket(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        TicketResponse response = ticketService.closeTicket(
                ticketId,
                userDetails.getId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}/history")
    @PreAuthorize("@ticketAuthorizationService.canAccessTicket(#ticketId, authentication)")
    public ResponseEntity<List<TicketHistoryResponse>> getTicketHistory(
            @PathVariable UUID ticketId
    ) {
        List<TicketHistoryResponse> response =
                ticketService.getTicketHistory(ticketId);

        return ResponseEntity.ok(response);
    }
}