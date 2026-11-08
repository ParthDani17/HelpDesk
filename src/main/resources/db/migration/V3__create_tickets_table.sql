CREATE TABLE tickets (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

     title VARCHAR(150) NOT NULL,
     description TEXT NOT NULL,

     priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
     status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

     creator_id UUID NOT NULL,
     assignee_id UUID,
     category_id UUID NOT NULL,

     version BIGINT NOT NULL DEFAULT 0,

     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

     CONSTRAINT fk_tickets_creator
         FOREIGN KEY (creator_id) REFERENCES users(id),

     CONSTRAINT fk_tickets_assignee
         FOREIGN KEY (assignee_id) REFERENCES users(id),

     CONSTRAINT fk_tickets_category
         FOREIGN KEY (category_id) REFERENCES categories(id),

     CONSTRAINT chk_tickets_priority
         CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

     CONSTRAINT chk_tickets_status
         CHECK (status IN (
                           'OPEN',
                           'ASSIGNED',
                           'IN_PROGRESS',
                           'RESOLVED',
                           'CLOSED',
                           'REOPENED'
             )),

     CONSTRAINT chk_tickets_title
         CHECK (LENGTH(TRIM(title)) > 0)
);

CREATE INDEX idx_tickets_creator_id ON tickets(creator_id);
CREATE INDEX idx_tickets_assignee_id ON tickets(assignee_id);
CREATE INDEX idx_tickets_category_id ON tickets(category_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);
