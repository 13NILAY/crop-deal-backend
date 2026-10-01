package com.cropdeal.profile.dto;

import com.cropdeal.profile.entity.FarmerProfile;
import com.cropdeal.profile.entity.VerificationStatus;

public record FarmerDirectoryResponse(Long id, String fullName, String location,
                                      VerificationStatus verificationStatus) {
    public static FarmerDirectoryResponse from(FarmerProfile profile) {
        return new FarmerDirectoryResponse(profile.getId(), profile.getFullName(),
                profile.getLocation(), profile.getVerificationStatus());
    }
}