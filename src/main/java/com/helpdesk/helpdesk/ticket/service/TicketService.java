package com.helpdesk.helpdesk.ticket.service;

import com.helpdesk.helpdesk.category.entity.Category;
import com.helpdesk.helpdesk.category.repository.CategoryRepository;
import com.helpdesk.helpdesk.common.exception.ConflictException;
import com.helpdesk.helpdesk.common.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.history.entity.TicketHistory;
import com.helpdesk.helpdesk.history.enums.TicketHistoryAction;
import com.helpdesk.helpdesk.history.repository.TicketHistoryRepository;
import com.helpdesk.helpdesk.ticket.dto.TicketCreateRequest;
import com.helpdesk.helpdesk.ticket.dto.TicketResponse;
import com.helpdesk.helpdesk.ticket.entity.Ticket;
import com.helpdesk.helpdesk.ticket.enums.TicketPriority;
import com.helpdesk.helpdesk.ticket.repository.TicketRepository;
import com.helpdesk.helpdesk.user.entity.User;
import com.helpdesk.helpdesk.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import com.helpdesk.helpdesk.user.enums.UserRole;
import com.helpdesk.helpdesk.ticket.enums.TicketStatus;
import java.util.List;

import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TicketHistoryRepository ticketHistoryRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request, UUID creatorId) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Active category not found"));

        Ticket ticket = new Ticket(
                request.title().trim(),
                request.description().trim(),
                request.priority(),
                creator,
                category
        );

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(
                savedTicket,
                creator,
                TicketHistoryAction.CREATED,
                null,
                "Ticket created with OPEN status"
        );

        ticketHistoryRepository.save(history);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getCreator().getId(),
                savedTicket.getAssignee() == null ? null : savedTicket.getAssignee().getId(),
                savedTicket.getCategory().getId(),
                savedTicket.getVersion(),
                savedTicket.getCreatedAt(),
                savedTicket.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreator().getId(),
                ticket.getAssignee() == null ? null : ticket.getAssignee().getId(),
                ticket.getCategory().getId(),
                ticket.getVersion(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTickets(HelpDeskUserDetails userDetails) {

        List<Ticket> tickets;

        if (userDetails.getRole() == UserRole.ADMIN) {
            tickets = ticketRepository.findAll();
        } else if (userDetails.getRole() == UserRole.AGENT) {
            tickets = ticketRepository.findAllByAssigneeId(userDetails.getId());
        } else {
            tickets = ticketRepository.findAllByCreatorId(userDetails.getId());
        }

        return tickets.stream()
                .map(ticket -> new TicketResponse(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getPriority(),
                        ticket.getStatus(),
                        ticket.getCreator().getId(),
                        ticket.getAssignee() == null ? null : ticket.getAssignee().getId(),
                        ticket.getCategory().getId(),
                        ticket.getVersion(),
                        ticket.getCreatedAt(),
                        ticket.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional
    public TicketResponse assignTicket(UUID ticketId, UUID agentId, UUID adminId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        if (agent.getRole() != UserRole.AGENT || !agent.isActive()) {
            throw new ConflictException("Selected user is not an active agent");
        }

        User previousAssignee = ticket.getAssignee();

        TicketHistoryAction action = previousAssignee == null
                ? TicketHistoryAction.ASSIGNED
                : TicketHistoryAction.REASSIGNED;

        String oldValue = previousAssignee == null
                ? null
                : previousAssignee.getEmail();

        String newValue = agent.getEmail();

        ticket.setAssignee(agent);

        Ticket savedTicket = ticketRepository.save(ticket);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        TicketHistory history = new TicketHistory(
                savedTicket,
                admin,
                action,
                oldValue,
                newValue
        );

        ticketHistoryRepository.save(history);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getCreator().getId(),
                savedTicket.getAssignee().getId(),
                savedTicket.getCategory().getId(),
                savedTicket.getVersion(),
                savedTicket.getCreatedAt(),
                savedTicket.getUpdatedAt()
        );
    }

    @Transactional
    public TicketResponse updateTicketStatus(UUID ticketId, TicketStatus newStatus, UUID performedById) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User performedBy = userRepository.findById(performedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TicketStatus oldStatus = ticket.getStatus();

        boolean validTransition = switch (oldStatus) {
            case OPEN -> newStatus == TicketStatus.ASSIGNED;

            case ASSIGNED -> newStatus == TicketStatus.IN_PROGRESS
                    || newStatus == TicketStatus.OPEN;

            case IN_PROGRESS -> newStatus == TicketStatus.RESOLVED;

            case RESOLVED -> newStatus == TicketStatus.CLOSED
                    || newStatus == TicketStatus.REOPENED;

            case REOPENED -> newStatus == TicketStatus.ASSIGNED
                    || newStatus == TicketStatus.IN_PROGRESS;

            case CLOSED -> false;
        };

        if (!validTransition) {
            throw new ConflictException(
                    "Invalid ticket status transition: " + oldStatus + " -> " + newStatus
            );
        }

        ticket.setStatus(newStatus);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(
                savedTicket,
                performedBy,
                newStatus == TicketStatus.REOPENED
                        ? TicketHistoryAction.REOPENED
                        : TicketHistoryAction.STATUS_CHANGED,
                oldStatus.name(),
                newStatus.name()
        );

        ticketHistoryRepository.save(history);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getCreator().getId(),
                savedTicket.getAssignee() == null ? null : savedTicket.getAssignee().getId(),
                savedTicket.getCategory().getId(),
                savedTicket.getVersion(),
                savedTicket.getCreatedAt(),
                savedTicket.getUpdatedAt()
        );
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreator().getId(),
                ticket.getAssignee() == null ? null : ticket.getAssignee().getId(),
                ticket.getCategory().getId(),
                ticket.getVersion(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    @Transactional
    public TicketResponse updateTicketPriority(
            UUID ticketId,
            TicketPriority newPriority,
            UUID performedById
    ) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User performedBy = userRepository.findById(performedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TicketPriority oldPriority = ticket.getPriority();

        if (oldPriority == newPriority) {
            throw new ConflictException("Ticket already has this priority");
        }

        ticket.setPriority(newPriority);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(
                savedTicket,
                performedBy,
                TicketHistoryAction.PRIORITY_CHANGED,
                oldPriority.name(),
                newPriority.name()
        );

        ticketHistoryRepository.save(history);

        return mapToTicketResponse(savedTicket);
    }

    @Transactional
    public TicketResponse updateTicketCategory(
            UUID ticketId,
            UUID categoryId,
            UUID performedById
    ) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User performedBy = userRepository.findById(performedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category newCategory = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Active category not found"));

        Category oldCategory = ticket.getCategory();

        if (oldCategory.getId().equals(newCategory.getId())) {
            throw new ConflictException("Ticket already belongs to this category");
        }

        ticket.setCategory(newCategory);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(
                savedTicket,
                performedBy,
                TicketHistoryAction.CATEGORY_CHANGED,
                oldCategory.getName(),
                newCategory.getName()
        );

        ticketHistoryRepository.save(history);

        return mapToTicketResponse(savedTicket);
    }

    @Transactional
    public TicketResponse reopenTicket(
            UUID ticketId,
            String reason,
            UUID performedById
    ) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User performedBy = userRepository.findById(performedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TicketStatus oldStatus = ticket.getStatus();

        if (oldStatus != TicketStatus.RESOLVED) {
            throw new ConflictException("Only resolved tickets can be reopened");
        }

        ticket.setStatus(TicketStatus.REOPENED);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(
                savedTicket,
                performedBy,
                TicketHistoryAction.REOPENED,
                oldStatus.name(),
                reason.trim()
        );

        ticketHistoryRepository.save(history);

        return mapToTicketResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public boolean isTicketCreator(UUID ticketId, UUID userId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> ticket.getCreator().getId().equals(userId))
                .orElse(false);
    }

    @Transactional
    public TicketResponse closeTicket(UUID ticketId, UUID performedById) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User performedBy = userRepository.findById(performedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TicketStatus oldStatus = ticket.getStatus();

        if (oldStatus != TicketStatus.RESOLVED) {
            throw new ConflictException("Only resolved tickets can be closed");
        }

        ticket.setStatus(TicketStatus.CLOSED);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketHistory history = new TicketHistory(
                savedTicket,
                performedBy,
                TicketHistoryAction.STATUS_CHANGED,
                oldStatus.name(),
                TicketStatus.CLOSED.name()
        );

        ticketHistoryRepository.save(history);

        return mapToTicketResponse(savedTicket);
    }
}