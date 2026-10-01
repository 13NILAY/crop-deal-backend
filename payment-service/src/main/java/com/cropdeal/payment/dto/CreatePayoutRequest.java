package com.cropdeal.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreatePayoutRequest(@NotNull Long orderId, @NotNull Long farmerId,
        @NotNull String farmerEmail, @NotNull Long bankAccountId,
        @NotNull @Positive BigDecimal grossAmount,
        @NotNull @Positive BigDecimal commissionAmount) { }