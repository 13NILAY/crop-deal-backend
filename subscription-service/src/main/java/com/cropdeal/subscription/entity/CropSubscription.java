package com.cropdeal.subscription.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"dealerEmail", "farmerId"}))
public class CropSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 254) private String dealerEmail;
    @Column(nullable = false) private Long farmerId;
    @Column(nullable = false) private LocalDateTime createdAt;

    public CropSubscription() { }
    public Long getId() { return id; }
    public String getDealerEmail() { return dealerEmail; }
    public void setDealerEmail(String value) { dealerEmail = value; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long value) { farmerId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
