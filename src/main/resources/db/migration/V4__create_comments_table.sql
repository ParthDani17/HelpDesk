CREATE TABLE comments (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

      ticket_id UUID NOT NULL,
      author_id UUID NOT NULL,

      content TEXT NOT NULL,

      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT fk_comments_ticket
          FOREIGN KEY (ticket_id) REFERENCES tickets(id),

      CONSTRAINT fk_comments_author
          FOREIGN KEY (author_id) REFERENCES users(id),

      CONSTRAINT chk_comments_content
          CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX idx_comments_ticket_id
    ON comments(ticket_id);

CREATE INDEX idx_comments_author_id
    ON comments(author_id);

CREATE INDEX idx_comments_created_at
    ON comments(created_at);
