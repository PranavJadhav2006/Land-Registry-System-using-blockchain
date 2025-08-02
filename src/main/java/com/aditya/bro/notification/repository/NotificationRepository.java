package com.aditya.bro.notification.repository;

import com.aditya.bro.notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientRoleAndReadFalse(String recipientRole);
    List<Notification> findByRecipientIdAndReadFalse(String recipientId);
    List<Notification> findByRecipientId(String recipientId);
    List<Notification> findByRecipientRole(String recipientRole);
}
