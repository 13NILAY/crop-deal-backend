package com.cropdeal.payment;

import static org.mockito.Mockito.*;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentDocument;
import com.cropdeal.payment.entity.PaymentDocumentType;
import com.cropdeal.payment.repository.PaymentDocumentRepository;
import com.cropdeal.payment.service.PaymentEmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class PaymentEmailServiceTest {
    @Test
    void sendsBothPaymentDocumentsAsAttachments() {
        PaymentDocumentRepository repository = mock(PaymentDocumentRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        Payment payment = new Payment();
        payment.setOrderId(42L);
        payment.setDealerEmail("dealer@example.com");
        payment.setAmount(new BigDecimal("125.00"));
        PaymentDocument invoice = document(PaymentDocumentType.INVOICE, "INV-42");
        PaymentDocument receipt = document(PaymentDocumentType.RECEIPT, "REC-42");
        when(repository.findByPaymentId(null)).thenReturn(List.of(invoice, receipt));

        new PaymentEmailService(repository, mailSender, true, "noreply@cropdeal.test")
                .sendPaymentDocuments(payment);

        verify(mailSender).send(message);
    }

    private PaymentDocument document(PaymentDocumentType type, String number) {
        PaymentDocument document = new PaymentDocument();
        document.setType(type);
        document.setDocumentNumber(number);
        document.setPdfData("pdf".getBytes());
        return document;
    }
}