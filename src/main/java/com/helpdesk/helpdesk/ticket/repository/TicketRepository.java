package com.helpdesk.helpdesk.ticket.repository;

import com.helpdesk.helpdesk.ticket.entity.Ticket;
import com.helpdesk.helpdesk.ticket.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.helpdesk.helpdesk.ticket.enums.TicketPriority;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByCreatorId(UUID creatorId);

    List<Ticket> findAllByAssigneeId(UUID assigneeId);

    List<Ticket> findAllByStatus(TicketStatus status);

    List<Ticket> findAllByCategoryId(UUID categoryId);

    Page<Ticket> findAllByCreatorId(UUID creatorId, Pageable pageable);

    Page<Ticket> findAllByAssigneeId(UUID assigneeId, Pageable pageable);

    @Query("""
        SELECT t FROM Ticket t
        WHERE (:creatorId IS NULL OR t.creator.id = :creatorId)
        AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        """)
    Page<Ticket> findTicketsWithFilters(
            @Param("creatorId") UUID creatorId,
            @Param("assigneeId") UUID assigneeId,
            @Param("status") TicketStatus status,
            @Param("priority") TicketPriority priority,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );
}
