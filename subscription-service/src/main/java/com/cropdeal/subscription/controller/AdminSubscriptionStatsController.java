package com.cropdeal.subscription.controller;

import com.cropdeal.subscription.repository.CropSubscriptionRepository;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin/stats")
public class AdminSubscriptionStatsController {
    private final CropSubscriptionRepository repository;

    public AdminSubscriptionStatsController(CropSubscriptionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        long activeSubscriptions = repository.count();
        return Map.of("totalSubscriptions", activeSubscriptions,
                "activeSubscriptions", activeSubscriptions);
    }
}