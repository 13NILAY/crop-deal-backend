package com.cropdeal.crop.dto;
 
import java.time.LocalDateTime;
 
import com.cropdeal.crop.model.CropStatus;
import com.cropdeal.crop.model.ListingStatus;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CropResponseDTO {
 
    private Long id;
 
    private Long farmerId;
 
    private String cropName;
 
    private String cropType;
 
    private Double quantity;
 
    private Double price;
 
    private String location;
 
    private CropStatus status;

    private ListingStatus listingStatus;
 
    private LocalDateTime createdAt;
}
 