package com.helpdesk.helpdesk.history.repository;

import com.helpdesk.helpdesk.history.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, UUID> {

    List<TicketHistory> findAllByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    List<TicketHistory> findAllByPerformedById(UUID userId);
}
