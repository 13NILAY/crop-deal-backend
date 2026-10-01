package com.cropdeal.payment.dto;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentResponse(Long id, Long orderId, String dealerEmail, BigDecimal amount,
                                   PaymentStatus status, LocalDateTime createdAt) {
    public static AdminPaymentResponse from(Payment payment) {
        return new AdminPaymentResponse(payment.getId(), payment.getOrderId(), payment.getDealerEmail(),
                payment.getAmount(), payment.getStatus(), payment.getCreatedAt());
    }
}