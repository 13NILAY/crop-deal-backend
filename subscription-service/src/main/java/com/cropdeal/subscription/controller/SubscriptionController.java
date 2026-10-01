package com.cropdeal.subscription.controller;

import com.cropdeal.subscription.entity.CropSubscription;
import com.cropdeal.subscription.service.SubscriptionService;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;
    private final String internalToken;

    public SubscriptionController(SubscriptionService service,
            @Value("${subscription.internal-token}") String internalToken) {
        this.service = service;
        this.internalToken = internalToken;
    }

    @PostMapping("/farmers/{farmerId}")
    public CropSubscription subscribe(@PathVariable Long farmerId, Principal principal) {
        return service.subscribe(principal.getName(), farmerId);
    }

    @DeleteMapping("/farmers/{farmerId}")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long farmerId, Principal principal) {
        service.unsubscribe(principal.getName(), farmerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public List<CropSubscription> mine(Principal principal) {
        return service.mine(principal.getName());
    }

    @GetMapping("/farmers/{farmerId}/dealers")
    public List<String> dealerEmails(@PathVariable Long farmerId,
            @RequestHeader("X-Internal-Token") String token) {
        if (!internalToken.equals(token)) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid internal token");
        }
        return service.dealerEmails(farmerId);
    }
}
