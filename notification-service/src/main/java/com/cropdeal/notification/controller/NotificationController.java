package com.cropdeal.notification.controller;

import com.cropdeal.notification.entity.Notification;
import com.cropdeal.notification.service.NotificationService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public List<Notification> list(Principal principal) { return service.list(principal.getName()); }
    @PutMapping("/{id}/read") public ResponseEntity<Void> markRead(@PathVariable Long id, Principal principal) {
        service.markRead(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}