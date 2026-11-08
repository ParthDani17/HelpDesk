package com.helpdesk.helpdesk.comment.repository;

import com.helpdesk.helpdesk.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    List<Comment> findAllByAuthorId(UUID authorId);
}
