CREATE TABLE notifications (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

       user_id UUID NOT NULL,
       ticket_id UUID,

       message VARCHAR(500) NOT NULL,
       type VARCHAR(50) NOT NULL,

       is_read BOOLEAN NOT NULL DEFAULT FALSE,

       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT fk_notifications_user
           FOREIGN KEY (user_id) REFERENCES users(id),

       CONSTRAINT fk_notifications_ticket
           FOREIGN KEY (ticket_id) REFERENCES tickets(id),

       CONSTRAINT chk_notifications_message
           CHECK (LENGTH(TRIM(message)) > 0)
);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_ticket_id
    ON notifications(ticket_id);

CREATE INDEX idx_notifications_user_read
    ON notifications(user_id, is_read);

CREATE INDEX idx_notifications_created_at
    ON notifications(created_at);
