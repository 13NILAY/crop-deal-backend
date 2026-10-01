package com.cropdeal.crop.controller;

import com.cropdeal.crop.model.CropStatus;
import com.cropdeal.crop.model.ListingStatus;
import com.cropdeal.crop.repository.CropRepository;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCropStatsController {
    private final CropRepository repository;

    public AdminCropStatsController(CropRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        return Map.of("totalCrops", repository.count(),
                "activeCrops", repository.countByListingStatus(ListingStatus.ACTIVE),
                "inactiveCrops", repository.countByListingStatus(ListingStatus.INACTIVE),
                "availableCrops", repository.countByListingStatusAndStatus(ListingStatus.ACTIVE, CropStatus.AVAILABLE),
                "soldCrops", repository.countByListingStatusAndStatus(ListingStatus.ACTIVE, CropStatus.SOLD));
    }
}