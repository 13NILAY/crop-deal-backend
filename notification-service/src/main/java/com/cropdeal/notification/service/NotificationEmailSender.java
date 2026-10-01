package com.cropdeal.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationEmailSender {
    private static final Logger log = LoggerFactory.getLogger(NotificationEmailSender.class);
    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public NotificationEmailSender(JavaMailSender mailSender,
            @Value("${notification.email.from:}") String from,
            @Value("${notification.email.enabled:false}") boolean enabled) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = enabled;
    }

    public void send(String recipient, String eventType, String message) {
        if (!enabled || recipient == null || !recipient.contains("@")) return;
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(recipient);
            mail.setSubject("CropDeal notification: " + eventType);
            mail.setText(message);
            mailSender.send(mail);
        } catch (RuntimeException ex) {
            log.warn("Could not send notification email to {}: {}", recipient, ex.getMessage());
        }
    }
}