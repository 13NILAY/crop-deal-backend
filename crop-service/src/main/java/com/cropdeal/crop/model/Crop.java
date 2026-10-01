package com.cropdeal.crop.model;
 
import java.time.LocalDateTime;
 
import org.hibernate.annotations.CreationTimestamp;
 
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Crop {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private Long farmerId;

    private String ownerEmail;
 
    private String cropName;
 
    private String cropType;
 
    private Double quantity;
 
    private Double price;
 
    private String location;
 
    @Enumerated(EnumType.STRING)
    private CropStatus status;

    @Enumerated(EnumType.STRING)
    @jakarta.persistence.Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null default 'ACTIVE'")
    private ListingStatus listingStatus = ListingStatus.ACTIVE;
 
    @CreationTimestamp
    private LocalDateTime createdAt;
}
 