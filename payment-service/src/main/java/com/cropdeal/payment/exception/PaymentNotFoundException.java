package com.cropdeal.payment.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) { super("Payment not found: " + id); }
    public PaymentNotFoundException(String razorpayOrderId) { super("Payment not found for Razorpay order: " + razorpayOrderId); }
}