package com.cropdeal.payment.event;

import com.cropdeal.payment.entity.PaymentStatus;
import java.math.BigDecimal;

public record PaymentStatusChangedEvent(Long paymentId, Long orderId, String dealerEmail,
                                        BigDecimal amount, PaymentStatus status) { }