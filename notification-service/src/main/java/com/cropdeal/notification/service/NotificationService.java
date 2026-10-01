package com.cropdeal.notification.service;

import com.cropdeal.notification.entity.Notification;
import com.cropdeal.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationEmailSender emailSender;
    public NotificationService(NotificationRepository repository, NotificationEmailSender emailSender) {
        this.repository = repository;
        this.emailSender = emailSender;
    }
    public void save(String recipientEmail, String eventType, String message) {
        String eventKey = hash(recipientEmail + "\n" + eventType + "\n" + message);
        if (repository.existsByEventKey(eventKey)) return;
        Notification notification = new Notification();
        notification.setRecipientEmail(recipientEmail);
        notification.setEventType(eventType);
        notification.setMessage(message);
        notification.setEventKey(eventKey);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadNotification(false);
        repository.save(notification);
        emailSender.send(recipientEmail, eventType, message);
    }
    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not fingerprint notification event", ex);
        }
    }
    public List<Notification> list(String recipientEmail) { return repository.findByRecipientEmailOrderByCreatedAtDesc(recipientEmail); }
    public void markRead(Long id, String recipientEmail) {
        Notification notification = repository.findByIdAndRecipientEmail(id, recipientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setReadNotification(true);
        repository.save(notification);
    }
}