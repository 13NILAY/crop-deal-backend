package com.cropdeal.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_documents", uniqueConstraints = @UniqueConstraint(columnNames = {"payment_id", "document_type"}))
public class PaymentDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long paymentId;
    @Column(nullable = false) private Long orderId;
    @Column(nullable = false, length = 254) private String dealerEmail;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(name = "document_type", nullable = false, length = 20)
    private PaymentDocumentType type;
    @Column(nullable = false, unique = true, length = 80) private String documentNumber;
    @Column(nullable = false) private LocalDateTime generatedAt;
    @Lob @Column(nullable = false) private byte[] pdfData;

    public PaymentDocument() { }
    public Long getId() { return id; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long value) { paymentId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getDealerEmail() { return dealerEmail; }
    public void setDealerEmail(String value) { dealerEmail = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public PaymentDocumentType getType() { return type; }
    public void setType(PaymentDocumentType value) { type = value; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String value) { documentNumber = value; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime value) { generatedAt = value; }
    public byte[] getPdfData() { return pdfData; }
    public void setPdfData(byte[] value) { pdfData = value; }
}