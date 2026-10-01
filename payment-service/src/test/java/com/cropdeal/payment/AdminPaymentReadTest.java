package com.cropdeal.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentStatus;
import com.cropdeal.payment.repository.PaymentRepository;
import com.cropdeal.payment.service.PaymentDocumentService;
import com.cropdeal.payment.service.PaymentEmailService;
import com.cropdeal.payment.service.PaymentEventPublisher;
import com.cropdeal.payment.service.PaymentService;
import com.razorpay.RazorpayClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AdminPaymentReadTest {
    @Mock PaymentRepository paymentRepository;
    @Mock PaymentEventPublisher eventPublisher;
    @Mock RazorpayClient razorpayClient;
    @Mock PaymentDocumentService documentService;
    @Mock PaymentEmailService emailService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, eventPublisher, razorpayClient,
                documentService, emailService);
    }

    @Test
    void adminSearchMapsPaymentsToSafeReadResponse() {
        Payment payment = new Payment();
        payment.setOrderId(9L);
        payment.setRazorpayOrderId("provider-order-id");
        payment.setRazorpayPaymentId("provider-payment-id");
        payment.setDealerEmail("dealer@example.com");
        payment.setAmount(new BigDecimal("15.00"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        when(paymentRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(payment)));

        var result = paymentService.adminSearch(PaymentStatus.SUCCESS, 9L, "dealer@example.com",
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertEquals(1, result.getTotalElements());
        assertEquals(9L, result.getContent().get(0).orderId());
        assertEquals(PaymentStatus.SUCCESS, result.getContent().get(0).status());
        verify(paymentRepository).findAll(any(Specification.class), any(PageRequest.class));
    }
}
