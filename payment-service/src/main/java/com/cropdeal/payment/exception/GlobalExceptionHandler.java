package com.cropdeal.payment.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PaymentNotFoundException.class) ResponseEntity<?> notFound(PaymentNotFoundException ex) { return body(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex.getMessage()); }
    @ExceptionHandler(PaymentProviderNotConfiguredException.class) ResponseEntity<?> providerNotConfigured(PaymentProviderNotConfiguredException ex) { return body(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_PROVIDER_NOT_CONFIGURED", ex.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> invalidPayment(IllegalArgumentException ex) { return body(HttpStatus.BAD_REQUEST, "PAYMENT_VERIFICATION_FAILED", ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).reduce((a, b) -> a + ", " + b).orElse("Invalid request");
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }
    private ResponseEntity<?> body(HttpStatus status, String error, String message) { return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now(), "status", status.value(), "error", error, "message", message)); }
}