package com.cropdeal.notification;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cropdeal.notification.entity.Notification;
import com.cropdeal.notification.repository.NotificationRepository;
import com.cropdeal.notification.service.NotificationEmailSender;
import com.cropdeal.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {
    @Test
    void doesNotSaveOrEmailTheSameEventTwice() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailSender emailSender = mock(NotificationEmailSender.class);
        when(repository.existsByEventKey(anyString())).thenReturn(false, true);
        NotificationService service = new NotificationService(repository, emailSender);

        service.save("dealer@example.com", "PAYMENT_STATUS_CHANGED", "paymentId=42");
        service.save("dealer@example.com", "PAYMENT_STATUS_CHANGED", "paymentId=42");

        verify(repository, times(1)).save(any(Notification.class));
        verify(emailSender, times(1)).send(anyString(), anyString(), anyString());
    }

    @Test
    void savesUniqueEventAndEmailsRecipient() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationEmailSender emailSender = mock(NotificationEmailSender.class);
        when(repository.existsByEventKey(anyString())).thenReturn(false);
        NotificationService service = new NotificationService(repository, emailSender);

        service.save("dealer@example.com", "ORDER_CREATED", "Order created");

        verify(repository).save(argThat(notification -> notification.getRecipientEmail().equals("dealer@example.com")
                && !notification.isReadNotification()));
        verify(emailSender).send("dealer@example.com", "ORDER_CREATED", "Order created");
    }

    @Test
    void listsNotificationsForRecipient() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification notification = notification();
        when(repository.findByRecipientEmailOrderByCreatedAtDesc("dealer@example.com"))
                .thenReturn(List.of(notification));
        NotificationService service = new NotificationService(repository, mock(NotificationEmailSender.class));

        assertEquals(List.of(notification), service.list("dealer@example.com"));
    }

    @Test
    void marksRecipientNotificationRead() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification notification = notification();
        when(repository.findByIdAndRecipientEmail(1L, "dealer@example.com"))
                .thenReturn(Optional.of(notification));
        NotificationService service = new NotificationService(repository, mock(NotificationEmailSender.class));

        service.markRead(1L, "dealer@example.com");

        assertTrue(notification.isReadNotification());
        verify(repository).save(notification);
    }

    @Test
    void doesNotMarkAnotherRecipientsNotificationRead() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.findByIdAndRecipientEmail(1L, "other@example.com")).thenReturn(Optional.empty());
        NotificationService service = new NotificationService(repository, mock(NotificationEmailSender.class));

        assertThrows(IllegalArgumentException.class, () -> service.markRead(1L, "other@example.com"));
        verify(repository, never()).save(any(Notification.class));
    }

    private Notification notification() {
        Notification notification = new Notification();
        notification.setRecipientEmail("dealer@example.com");
        notification.setEventType("ORDER_CREATED");
        notification.setMessage("Order created");
        notification.setEventKey("event-key");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}