package com.cropdeal.order.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(OrderNotFoundException.class)
	ResponseEntity<?> notFound(OrderNotFoundException ex) {
		return body(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage());
	}

	@ExceptionHandler(CropUnavailableException.class)
	ResponseEntity<?> unavailable(CropUnavailableException ex) {
		return body(HttpStatus.CONFLICT, "CROP_UNAVAILABLE", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<?> invalid(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.reduce((first, second) -> first + ", " + second).orElse("Invalid request");
		return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
	}

	private ResponseEntity<?> body(HttpStatus status, String error, String message) {
		return ResponseEntity.status(status)
				.body(Map.of("timestamp", Instant.now(), "status", status.value(), "error", error, "message", message));
	}
}