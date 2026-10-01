package com.cropdeal.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentDocument;
import com.cropdeal.payment.entity.PaymentDocumentType;
import com.cropdeal.payment.repository.PaymentDocumentRepository;
import com.cropdeal.payment.service.PaymentDocumentService;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PaymentDocumentServiceTest {
    private final PaymentDocumentRepository repository = mock(PaymentDocumentRepository.class);
    private final PaymentDocumentService service = new PaymentDocumentService(repository);
    private final Map<PaymentDocumentType, PaymentDocument> documents = new EnumMap<>(PaymentDocumentType.class);

    @Test
    void createsInvoiceAndReceiptOnlyOnce() {
        Payment payment = new Payment();
        payment.setOrderId(42L);
        payment.setDealerEmail("dealer@example.com");
        payment.setAmount(new BigDecimal("125.00"));
        when(repository.findByPaymentIdAndType(any(), any())).thenAnswer(invocation ->
            Optional.ofNullable(documents.get(invocation.getArgument(1))));
        when(repository.save(any(PaymentDocument.class))).thenAnswer(invocation -> {
            PaymentDocument document = invocation.getArgument(0);
            documents.put(document.getType(), document);
            return document;
        });

        service.createForSuccessfulPayment(payment);
        service.createForSuccessfulPayment(payment);

        verify(repository, times(4)).findByPaymentIdAndType(any(), any());
        verify(repository, times(2)).save(any(PaymentDocument.class));
    }
}