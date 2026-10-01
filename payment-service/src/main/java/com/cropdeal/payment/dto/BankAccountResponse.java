package com.cropdeal.payment.dto;

public record BankAccountResponse(Long id, String accountHolderName, String maskedAccountNumber,
                                  String maskedIfscCode, String bankName, boolean defaultAccount,
                                  String verificationStatus) { }