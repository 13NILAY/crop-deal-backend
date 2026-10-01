package com.cropdeal.payment.service;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentDocument;
import com.cropdeal.payment.repository.PaymentDocumentRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class PaymentEmailService {
    private static final Logger log = LoggerFactory.getLogger(PaymentEmailService.class);
    private final PaymentDocumentRepository documentRepository;
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public PaymentEmailService(PaymentDocumentRepository documentRepository, JavaMailSender mailSender,
                               @Value("${payment.email.enabled:false}") boolean enabled,
                               @Value("${payment.email.from:noreply@cropdeal.local}") String from) {
        this.documentRepository = documentRepository;
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void sendPaymentDocuments(Payment payment) {
        if (!enabled) {
            log.debug("Payment email disabled; skipping payment {}", payment.getId());
            return;
        }
        try {
            List<PaymentDocument> documents = documentRepository.findByPaymentId(payment.getId());
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(payment.getDealerEmail());
            helper.setSubject("CropDeal payment " + payment.getId() + " documents");
            helper.setText("Your CropDeal payment was successful. Invoice and receipt are attached.\n"
                    + "Order ID: " + payment.getOrderId() + "\nAmount: INR " + payment.getAmount());
            documents.forEach(document -> addAttachment(helper, document));
            mailSender.send(message);
            log.info("Payment documents emailed for payment {} to {}", payment.getId(), payment.getDealerEmail());
        } catch (Exception ex) {
            log.warn("Payment {} succeeded but documents could not be emailed: {}", payment.getId(), ex.getMessage());
        }
    }

    private void addAttachment(MimeMessageHelper helper, PaymentDocument document) {
        try {
            helper.addAttachment(document.getDocumentNumber() + ".pdf", new ByteArrayResource(document.getPdfData()),
                    "application/pdf");
        } catch (Exception ex) {
            throw new IllegalStateException("Could not attach payment document", ex);
        }
    }
}