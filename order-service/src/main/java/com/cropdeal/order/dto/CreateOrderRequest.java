package com.cropdeal.order.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;

public record CreateOrderRequest(
        @NotNull Long cropId,
        @NotNull Long farmerId,
        @NotNull @Positive @Digits(integer = 17, fraction = 3) BigDecimal quantity) { }