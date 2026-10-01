package com.cropdeal.order.dto;

import com.cropdeal.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(Long id, Long cropId, Long farmerId, String dealerEmail, BigDecimal quantity,
                            BigDecimal unitPrice, BigDecimal totalPrice, OrderStatus status,
                            LocalDateTime createdAt) { }