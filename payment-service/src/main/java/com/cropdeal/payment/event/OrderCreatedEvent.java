package com.cropdeal.payment.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, Long cropId, Long farmerId, String dealerEmail,
                                BigDecimal quantity, BigDecimal totalPrice) { }