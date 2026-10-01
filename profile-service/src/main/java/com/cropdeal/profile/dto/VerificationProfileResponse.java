package com.cropdeal.profile.dto;

import com.cropdeal.profile.entity.DealerProfile;
import com.cropdeal.profile.entity.FarmerProfile;
import com.cropdeal.profile.entity.VerificationStatus;

public record VerificationProfileResponse(Long id, String email, String displayName,
                                          String location, VerificationStatus status) {
    public static VerificationProfileResponse from(FarmerProfile profile) {
        return new VerificationProfileResponse(profile.getId(), profile.getUserEmail(),
                profile.getFullName(), profile.getLocation(), profile.getVerificationStatus());
    }

    public static VerificationProfileResponse from(DealerProfile profile) {
        return new VerificationProfileResponse(profile.getId(), profile.getUserEmail(),
                profile.getBusinessName(), profile.getLocation(), profile.getVerificationStatus());
    }
}