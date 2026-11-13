package com.helpdesk.helpdesk.comment.controller;

import com.helpdesk.helpdesk.comment.dto.CommentCreateRequest;
import com.helpdesk.helpdesk.comment.dto.CommentResponse;
import com.helpdesk.helpdesk.comment.service.CommentService;
import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("@ticketAuthorizationService.canAccessTicket(#ticketId, authentication)")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal HelpDeskUserDetails userDetails
    ) {
        CommentResponse response = commentService.addComment(
                ticketId,
                request,
                userDetails.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("@ticketAuthorizationService.canAccessTicket(#ticketId, authentication)")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID ticketId
    ) {
        List<CommentResponse> response = commentService.getComments(ticketId);
        return ResponseEntity.ok(response);
    }
}