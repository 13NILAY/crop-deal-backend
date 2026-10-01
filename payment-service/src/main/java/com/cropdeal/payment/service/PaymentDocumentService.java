package com.cropdeal.payment.service;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentDocument;
import com.cropdeal.payment.entity.PaymentDocumentType;
import com.cropdeal.payment.repository.PaymentDocumentRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class PaymentDocumentService {
    private static final DateTimeFormatter NUMBER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final PaymentDocumentRepository repository;

    public PaymentDocumentService(PaymentDocumentRepository repository) {
        this.repository = repository;
    }

    public void createForSuccessfulPayment(Payment payment) {
        createIfMissing(payment, PaymentDocumentType.INVOICE);
        createIfMissing(payment, PaymentDocumentType.RECEIPT);
    }

    public PaymentDocument findForDealer(Long paymentId, PaymentDocumentType type, String dealerEmail) {
        return repository.findByPaymentIdAndTypeAndDealerEmail(paymentId, type, dealerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Payment document not found"));
    }

    private void createIfMissing(Payment payment, PaymentDocumentType type) {
        if (repository.findByPaymentIdAndType(payment.getId(), type).isPresent()) return;
        LocalDateTime generatedAt = LocalDateTime.now();
        PaymentDocument document = new PaymentDocument();
        document.setPaymentId(payment.getId());
        document.setOrderId(payment.getOrderId());
        document.setDealerEmail(payment.getDealerEmail());
        document.setAmount(payment.getAmount());
        document.setType(type);
        document.setDocumentNumber((type == PaymentDocumentType.INVOICE ? "INV-" : "REC-")
                + payment.getId() + "-" + generatedAt.format(NUMBER_TIME));
        document.setGeneratedAt(generatedAt);
        document.setPdfData(renderPdf(document));
        repository.save(document);
    }

    private byte[] renderPdf(PaymentDocument document) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document pdf = new Document();
            PdfWriter.getInstance(pdf, output);
            pdf.open();
            pdf.add(new Paragraph("CropDeal " + document.getType()));
            pdf.add(new Paragraph("Document number: " + document.getDocumentNumber()));
            pdf.add(new Paragraph("Payment ID: " + document.getPaymentId()));
            pdf.add(new Paragraph("Order ID: " + document.getOrderId()));
            pdf.add(new Paragraph("Dealer: " + document.getDealerEmail()));
            pdf.add(new Paragraph("Amount: INR " + document.getAmount()));
            pdf.add(new Paragraph("Generated: " + document.getGeneratedAt()));
            pdf.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate payment document", ex);
        }
    }
}