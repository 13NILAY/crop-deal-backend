package com.cropdeal.crop.dto;

import com.cropdeal.crop.model.ListingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateListingStatusRequest(@NotNull ListingStatus status) {
}