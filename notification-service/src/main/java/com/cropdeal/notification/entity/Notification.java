package com.cropdeal.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 254) private String recipientEmail;
    @Column(nullable = false, length = 50) private String eventType;
    @Column(nullable = false, length = 4000) private String message;
    @Column(nullable = false, unique = true, length = 64) private String eventKey;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private boolean readNotification;
    public Notification() { }
    public Long getId() { return id; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String value) { recipientEmail = value; }
    public String getEventType() { return eventType; }
    public void setEventType(String value) { eventType = value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { message = value; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String value) { eventKey = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public boolean isReadNotification() { return readNotification; }
    public void setReadNotification(boolean value) { readNotification = value; }
}