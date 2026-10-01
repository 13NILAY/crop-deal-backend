package com.cropdeal.order.controller;

import com.cropdeal.order.dto.CreateOrderRequest;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    @PreAuthorize("hasRole('DEALER')")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request, Principal principal,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(principal.getName(), authorization, request));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DEALER')")
    public OrderResponse get(@PathVariable Long id, Principal principal) {
        return orderService.getForDealer(id, principal.getName());
    }
    @GetMapping("/dealer/me") public List<OrderResponse> dealerOrders(Principal principal) {
        return orderService.byDealer(principal.getName());
    }
    @GetMapping("/farmer/{farmerId}")
    @PreAuthorize("hasRole('FARMER')")
    public List<OrderResponse> farmerOrders(@PathVariable Long farmerId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return orderService.byFarmerForUser(farmerId, authorization);
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DEALER')")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestParam OrderStatus status, Principal principal) {
        return orderService.updateStatus(id, status, principal.getName());
    }
}