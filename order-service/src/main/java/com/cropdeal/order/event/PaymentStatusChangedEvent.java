package com.cropdeal.order.event;

public record PaymentStatusChangedEvent(Long paymentId, Long orderId, String dealerEmail,
                                        java.math.BigDecimal amount, String status) { }