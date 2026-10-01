package com.cropdeal.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record CreatePaymentRequest(@NotNull Long orderId,
								   @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount) { }