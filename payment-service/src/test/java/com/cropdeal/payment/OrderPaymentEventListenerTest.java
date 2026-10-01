package com.cropdeal.payment;

import static org.mockito.Mockito.*;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.event.OrderCreatedEvent;
import com.cropdeal.payment.repository.PaymentRepository;
import com.cropdeal.payment.service.OrderPaymentEventListener;
import com.cropdeal.payment.service.PaymentService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrderPaymentEventListenerTest {
    @Test
    void ignoresDuplicateOrderEventWhenPaymentAlreadyExists() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentRepository repository = mock(PaymentRepository.class);
        when(repository.findFirstByOrderIdOrderByCreatedAtDesc(42L)).thenReturn(Optional.of(new Payment()));
        OrderPaymentEventListener listener = new OrderPaymentEventListener(paymentService, repository);

        listener.onOrderCreated(new OrderCreatedEvent(42L, 7L, 9L, "dealer@example.com",
                new BigDecimal("2.00"), new BigDecimal("100.00")));

        verifyNoInteractions(paymentService);
    }

    @Test
    void createsPaymentForFirstOrderEvent() {
        PaymentService paymentService = mock(PaymentService.class);
        PaymentRepository repository = mock(PaymentRepository.class);
        when(repository.findFirstByOrderIdOrderByCreatedAtDesc(42L)).thenReturn(Optional.empty());
        OrderPaymentEventListener listener = new OrderPaymentEventListener(paymentService, repository);

        listener.onOrderCreated(new OrderCreatedEvent(42L, 7L, 9L, "dealer@example.com",
                new BigDecimal("2.00"), new BigDecimal("100.00")));

        verify(paymentService).create("dealer@example.com",
                new com.cropdeal.payment.dto.CreatePaymentRequest(42L, new BigDecimal("100.00")));
    }
}