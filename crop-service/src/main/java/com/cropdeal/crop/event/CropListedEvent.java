package com.cropdeal.crop.event;

public record CropListedEvent(
        Long cropId,
        Long farmerId,
        String farmerEmail,
        String cropName,
        String cropType,
        Double quantity,
        Double price,
        String location,
        String action) {
}