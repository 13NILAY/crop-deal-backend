package com.cropdeal.profile.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dealer_profiles")
public class DealerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 254) private String userEmail;
    @Column(nullable = false, length = 150) private String businessName;
    @Column(nullable = false, length = 100) private String contactName;
    @Column(nullable = false, length = 30) private String phoneNumber;
    @Column(nullable = false, length = 255) private String address;
    @Column(nullable = false, length = 100) private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null default 'PENDING'")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    public DealerProfile() { }
    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; } public void setUserEmail(String v) { userEmail = v; }
    public String getBusinessName() { return businessName; } public void setBusinessName(String v) { businessName = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { contactName = v; }
    public String getPhoneNumber() { return phoneNumber; } public void setPhoneNumber(String v) { phoneNumber = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getLocation() { return location; } public void setLocation(String v) { location = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
}
