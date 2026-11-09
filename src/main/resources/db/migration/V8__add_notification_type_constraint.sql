ALTER TABLE notifications
ADD CONSTRAINT chk_notifications_type
CHECK (
    type IN (
         'TICKET_CREATED',
         'TICKET_ASSIGNED',
         'TICKET_REASSIGNED',
         'COMMENT_ADDED',
         'STATUS_CHANGED',
         'TICKET_RESOLVED',
         'TICKET_REOPENED'
        )
);