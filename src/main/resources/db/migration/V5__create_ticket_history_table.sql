CREATE TABLE ticket_history (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

        ticket_id UUID NOT NULL,
        performed_by UUID,

        action VARCHAR(50) NOT NULL,

        old_value TEXT,
        new_value TEXT,

        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT fk_ticket_history_ticket
            FOREIGN KEY (ticket_id) REFERENCES tickets(id),

        CONSTRAINT fk_ticket_history_user
            FOREIGN KEY (performed_by) REFERENCES users(id),

        CONSTRAINT chk_ticket_history_action
            CHECK (action IN (
                'CREATED',
                'ASSIGNED',
                'REASSIGNED',
                'STATUS_CHANGED',
                'PRIORITY_CHANGED',
                'CATEGORY_CHANGED',
                'REOPENED'
                ))
    );

CREATE INDEX idx_ticket_history_ticket_id
    ON ticket_history(ticket_id);

CREATE INDEX idx_ticket_history_performed_by
    ON ticket_history(performed_by);

CREATE INDEX idx_ticket_history_created_at
    ON ticket_history(created_at);
