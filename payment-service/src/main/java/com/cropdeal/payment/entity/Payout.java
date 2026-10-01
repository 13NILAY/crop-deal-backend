package com.cropdeal.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
public class Payout {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long orderId;
    @Column(nullable = false) private Long farmerId;
    @Column(nullable = false, length = 254) private String farmerEmail;
    @Column(nullable = false) private Long bankAccountId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal grossAmount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal commissionAmount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal farmerAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PayoutStatus status;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(length = 100) private String razorpayTransferId;
    public Payout() { }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long value) { farmerId = value; }
    public String getFarmerEmail() { return farmerEmail; }
    public void setFarmerEmail(String value) { farmerEmail = value; }
    public Long getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(Long value) { bankAccountId = value; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal value) { grossAmount = value; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal value) { commissionAmount = value; }
    public BigDecimal getFarmerAmount() { return farmerAmount; }
    public void setFarmerAmount(BigDecimal value) { farmerAmount = value; }
    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public String getRazorpayTransferId() { return razorpayTransferId; }
    public void setRazorpayTransferId(String value) { razorpayTransferId = value; }
}