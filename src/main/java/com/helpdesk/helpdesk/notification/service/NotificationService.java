package com.helpdesk.helpdesk.notification.service;

import com.helpdesk.helpdesk.notification.dto.NotificationResponse;
import com.helpdesk.helpdesk.notification.entity.Notification;
import com.helpdesk.helpdesk.notification.enums.NotificationType;
import com.helpdesk.helpdesk.notification.repository.NotificationRepository;
import com.helpdesk.helpdesk.ticket.entity.Ticket;
import com.helpdesk.helpdesk.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(
            User user,
            Ticket ticket,
            String message,
            NotificationType type
    ) {
        Notification notification = new Notification(
                user,
                ticket,
                message,
                type
        );

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID userId) {

        List<Notification> notifications =
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getTicket() == null
                                ? null : notification.getTicket().getId(),
                        notification.getMessage(),
                        notification.getType(),
                        notification.isRead(),
                        notification.getCreatedAt()
                ))
                .toList();
    }
}
