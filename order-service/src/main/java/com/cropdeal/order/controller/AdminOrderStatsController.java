package com.cropdeal.order.controller;

import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.repository.OrderRepository;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderStatsController {
    private final OrderRepository repository;

    public AdminOrderStatsController(OrderRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        return Map.of("totalOrders", repository.count(),
                "createdOrders", repository.countByStatus(OrderStatus.CREATED),
                "pendingPaymentOrders", repository.countByStatus(OrderStatus.PENDING_PAYMENT),
                "completedOrders", repository.countByStatus(OrderStatus.DELIVERED),
                "cancelledOrders", repository.countByStatus(OrderStatus.CANCELLED));
    }
}