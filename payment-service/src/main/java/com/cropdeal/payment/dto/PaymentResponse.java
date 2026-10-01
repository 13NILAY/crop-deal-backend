package com.cropdeal.payment.dto;

import com.cropdeal.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long orderId, String dealerEmail, BigDecimal amount,
                              PaymentStatus status, String razorpayOrderId, String razorpayPaymentId,
                              LocalDateTime createdAt) { }