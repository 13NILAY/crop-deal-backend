package com.cropdeal.order.controller;

import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.service.OrderService;
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
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderResponse> list(@RequestParam(required = false) OrderStatus status,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(required = false) Long farmerId,
                                    @RequestParam(required = false) String dealerEmail,
                                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                    Pageable pageable) {
        return orderService.adminSearch(status, search, farmerId, dealerEmail, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }
}