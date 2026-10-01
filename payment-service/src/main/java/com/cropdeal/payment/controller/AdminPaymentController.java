package com.cropdeal.payment.controller;

import com.cropdeal.payment.dto.AdminPaymentResponse;
import com.cropdeal.payment.entity.PaymentStatus;
import com.cropdeal.payment.service.PaymentService;
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
@RequestMapping("/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {
    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public Page<AdminPaymentResponse> list(@RequestParam(required = false) PaymentStatus status,
                                           @RequestParam(required = false) Long orderId,
                                           @RequestParam(required = false) String search,
                                           @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                           Pageable pageable) {
        return paymentService.adminSearch(status, orderId, search, pageable);
    }

    @GetMapping("/{id}")
    public AdminPaymentResponse get(@PathVariable Long id) {
        return paymentService.adminGet(id);
    }
}