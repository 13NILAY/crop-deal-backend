package com.cropdeal.payment.dto;

import com.cropdeal.payment.entity.PayoutStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayoutResponse(Long id, Long orderId, Long farmerId, String farmerEmail, Long bankAccountId,
                             BigDecimal grossAmount, BigDecimal commissionAmount, BigDecimal farmerAmount,
                             PayoutStatus status, LocalDateTime createdAt) { }