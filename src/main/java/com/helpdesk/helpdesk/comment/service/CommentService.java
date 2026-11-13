package com.helpdesk.helpdesk.comment.service;

import com.helpdesk.helpdesk.comment.dto.CommentCreateRequest;
import com.helpdesk.helpdesk.comment.dto.CommentResponse;
import com.helpdesk.helpdesk.comment.entity.Comment;
import com.helpdesk.helpdesk.comment.repository.CommentRepository;
import com.helpdesk.helpdesk.common.exception.ConflictException;
import com.helpdesk.helpdesk.common.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.ticket.entity.Ticket;
import com.helpdesk.helpdesk.ticket.enums.TicketStatus;
import com.helpdesk.helpdesk.ticket.repository.TicketRepository;
import com.helpdesk.helpdesk.user.entity.User;
import com.helpdesk.helpdesk.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse addComment(
            UUID ticketId,
            CommentCreateRequest request,
            UUID authorId
    ) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ConflictException("Cannot comment on a closed ticket");
        }

        Comment comment = new Comment(
                ticket,
                author,
                request.content().trim()
        );

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponse(
                savedComment.getId(),
                ticket.getId(),
                author.getId(),
                author.getName(),
                savedComment.getContent(),
                savedComment.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID ticketId) {

        return commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getTicket().getId(),
                        comment.getAuthor().getId(),
                        comment.getAuthor().getName(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .toList();
    }
}