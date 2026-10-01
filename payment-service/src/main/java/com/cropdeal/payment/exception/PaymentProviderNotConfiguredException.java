package com.cropdeal.payment.exception;

public class PaymentProviderNotConfiguredException extends RuntimeException {
    public PaymentProviderNotConfiguredException() {
        super("Razorpay credentials are not configured");
    }
}