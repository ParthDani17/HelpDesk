package com.helpdesk.helpdesk.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorId,
        String authorName,
        String content,
        Instant createdAt
) {}