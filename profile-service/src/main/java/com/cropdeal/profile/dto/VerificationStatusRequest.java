package com.cropdeal.profile.dto;

import com.cropdeal.profile.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public record VerificationStatusRequest(@NotNull VerificationStatus status) {
}