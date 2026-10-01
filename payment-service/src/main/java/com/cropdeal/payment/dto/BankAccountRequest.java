package com.cropdeal.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BankAccountRequest(@NotBlank String accountHolderName,
        @NotBlank @Pattern(regexp = "^[0-9]{6,30}$") String accountNumber,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$") String ifscCode,
        @NotBlank String bankName, boolean defaultAccount, String razorpayLinkedAccountId) { }