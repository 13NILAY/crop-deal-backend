package com.cropdeal.payment.controller;

import com.cropdeal.payment.entity.PaymentStatus;
import com.cropdeal.payment.entity.PayoutStatus;
import com.cropdeal.payment.repository.PaymentRepository;
import com.cropdeal.payment.repository.PayoutRepository;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentStatsController {
    private final PaymentRepository paymentRepository;
    private final PayoutRepository payoutRepository;

    public AdminPaymentStatsController(PaymentRepository paymentRepository, PayoutRepository payoutRepository) {
        this.paymentRepository = paymentRepository;
        this.payoutRepository = payoutRepository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        return Map.of("totalPayments", paymentRepository.count(),
                "successfulPayments", paymentRepository.countByStatus(PaymentStatus.SUCCESS),
                "failedPayments", paymentRepository.countByStatus(PaymentStatus.FAILED),
                "pendingPayments", paymentRepository.countByStatus(PaymentStatus.PENDING),
                "pendingPayouts", payoutRepository.countByStatus(PayoutStatus.PENDING),
                "processingPayouts", payoutRepository.countByStatus(PayoutStatus.PROCESSING),
                "completedPayouts", payoutRepository.countByStatus(PayoutStatus.PAID),
                "failedPayouts", payoutRepository.countByStatus(PayoutStatus.FAILED));
    }
}