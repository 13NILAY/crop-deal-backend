package com.cropdeal.profile.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "farmer_profiles")
public class FarmerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 254) private String userEmail;
    @Column(nullable = false, length = 100) private String fullName;
    @Column(nullable = false, length = 30) private String phoneNumber;
    @Column(nullable = false, length = 255) private String address;
    @Column(nullable = false, length = 100) private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null default 'PENDING'")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    public FarmerProfile() { }
    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; } public void setUserEmail(String v) { userEmail = v; }
    public String getFullName() { return fullName; } public void setFullName(String v) { fullName = v; }
    public String getPhoneNumber() { return phoneNumber; } public void setPhoneNumber(String v) { phoneNumber = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getLocation() { return location; } public void setLocation(String v) { location = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
}
