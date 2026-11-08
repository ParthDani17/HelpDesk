package com.helpdesk.helpdesk.ticket.repository;

import com.helpdesk.helpdesk.ticket.entity.Ticket;
import com.helpdesk.helpdesk.ticket.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByCreatorId(UUID creatorId);

    List<Ticket> findAllByAssigneeId(UUID assigneeId);

    List<Ticket> findAllByStatus(TicketStatus status);

    List<Ticket> findAllByCategoryId(UUID categoryId);
}
