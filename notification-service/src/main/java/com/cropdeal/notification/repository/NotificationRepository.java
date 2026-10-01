package com.cropdeal.notification.repository;

import com.cropdeal.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByEventKey(String eventKey);
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    java.util.Optional<Notification> findByIdAndRecipientEmail(Long id, String recipientEmail);
}