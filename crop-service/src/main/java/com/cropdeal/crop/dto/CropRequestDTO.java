package com.cropdeal.crop.dto;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CropRequestDTO {
 
    @NotNull(message = "Farmer ID is required")
    private Long farmerId;
 
    @NotBlank(message = "Crop name is required")
    private String cropName;
 
    @NotBlank(message = "Crop type is required")
    private String cropType;
 
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Double quantity;
 
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;
 
    @NotBlank(message = "Location is required")
    private String location;
}
 