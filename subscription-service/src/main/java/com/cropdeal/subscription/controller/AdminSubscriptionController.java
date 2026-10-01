package com.cropdeal.subscription.controller;

import com.cropdeal.subscription.entity.CropSubscription;
import com.cropdeal.subscription.service.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/subscriptions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {
    private final SubscriptionService service;

    public AdminSubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CropSubscription> list(@RequestParam(required = false) Long farmerId,
                                       @RequestParam(required = false) String dealerEmail,
                                       @RequestParam(required = false) String status,
                                       @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                       Pageable pageable) {
        return service.adminSearch(farmerId, dealerEmail, status, pageable);
    }

    @GetMapping("/{id}")
    public CropSubscription get(@PathVariable Long id) {
        return service.adminGet(id);
    }
}