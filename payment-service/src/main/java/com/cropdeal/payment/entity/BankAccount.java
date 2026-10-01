package com.cropdeal.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 254) private String userEmail;
    @Column(nullable = false, length = 150) private String accountHolderName;
    @Column(nullable = false, length = 512) private String encryptedAccountNumber;
    @Column(nullable = false, length = 512) private String encryptedIfscCode;
    @Column(nullable = false, length = 150) private String bankName;
    @Column(nullable = false) private boolean defaultAccount;
    @Column(nullable = false, length = 20) private String verificationStatus;
    @Column(length = 100) private String razorpayLinkedAccountId;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    public BankAccount() { }
    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String value) { userEmail = value; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String value) { accountHolderName = value; }
    public String getEncryptedAccountNumber() { return encryptedAccountNumber; }
    public void setEncryptedAccountNumber(String value) { encryptedAccountNumber = value; }
    public String getEncryptedIfscCode() { return encryptedIfscCode; }
    public void setEncryptedIfscCode(String value) { encryptedIfscCode = value; }
    public String getBankName() { return bankName; }
    public void setBankName(String value) { bankName = value; }
    public boolean isDefaultAccount() { return defaultAccount; }
    public void setDefaultAccount(boolean value) { defaultAccount = value; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String value) { verificationStatus = value; }
    public String getRazorpayLinkedAccountId() { return razorpayLinkedAccountId; }
    public void setRazorpayLinkedAccountId(String value) { razorpayLinkedAccountId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}