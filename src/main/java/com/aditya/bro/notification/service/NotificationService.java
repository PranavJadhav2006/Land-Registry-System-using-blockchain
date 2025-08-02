package com.aditya.bro.notification.service;

import com.aditya.bro.notification.entity.Notification;
import com.aditya.bro.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(String message, String type, String recipientRole, String recipientId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipientRole(recipientRole);
        notification.setRecipientId(recipientId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAdminNotifications() {
        return notificationRepository.findByRecipientRole("ADMIN");
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByRecipientId(userId);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
