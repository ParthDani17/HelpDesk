package com.helpdesk.helpdesk.security.service;

import com.helpdesk.helpdesk.ticket.entity.Ticket;
import com.helpdesk.helpdesk.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import com.helpdesk.helpdesk.user.enums.UserRole;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Service
public class TicketAuthorizationService {

    private final TicketRepository ticketRepository;

    public TicketAuthorizationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public boolean isCreator(UUID ticketId, UUID userId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> ticket.getCreator().getId().equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAssignee(UUID ticketId, UUID userId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> ticket.getAssignee() != null
                        && ticket.getAssignee().getId().equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessTicket(UUID ticketId, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof HelpDeskUserDetails userDetails)) {
            return false;
        }

        UUID userId = userDetails.getId();
        UserRole role = userDetails.getRole();

        if (role == UserRole.ADMIN) {
            return ticketRepository.existsById(ticketId);
        }

        if (role == UserRole.USER) {
            return isCreator(ticketId, userId);
        }

        if (role == UserRole.AGENT) {
            return isAssignee(ticketId, userId);
        }

        return false;
    }
}