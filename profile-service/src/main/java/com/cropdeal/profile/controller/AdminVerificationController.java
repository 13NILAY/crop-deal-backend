package com.cropdeal.profile.controller;

import com.cropdeal.profile.dto.VerificationProfileResponse;
import com.cropdeal.profile.dto.VerificationStatusRequest;
import com.cropdeal.profile.entity.VerificationStatus;
import com.cropdeal.profile.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import com.cropdeal.profile.service.AdminActivityClient;

@RestController
@RequestMapping("/admin/verifications")
public class AdminVerificationController {
    private final ProfileService profileService;
    private final AdminActivityClient activityClient;

    public AdminVerificationController(ProfileService profileService, AdminActivityClient activityClient) {
        this.profileService = profileService;
        this.activityClient = activityClient;
    }

    @GetMapping("/farmers")
    public List<VerificationProfileResponse> farmers(
            @RequestParam(defaultValue = "PENDING") VerificationStatus status) {
        return profileService.farmersByVerification(status).stream()
                .map(VerificationProfileResponse::from).toList();
    }

    @GetMapping("/dealers")
    public List<VerificationProfileResponse> dealers(
            @RequestParam(defaultValue = "PENDING") VerificationStatus status) {
        return profileService.dealersByVerification(status).stream()
                .map(VerificationProfileResponse::from).toList();
    }

    @PatchMapping("/farmers/{id}")
    public VerificationProfileResponse updateFarmer(@PathVariable Long id,
                                                     @Valid @RequestBody VerificationStatusRequest request,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        var updated = profileService.updateFarmerVerification(id, request.status());
        activityClient.record(authorization, "FARMER_PROFILE", id, request.status().name());
        return VerificationProfileResponse.from(updated);
    }

    @PatchMapping("/dealers/{id}")
    public VerificationProfileResponse updateDealer(@PathVariable Long id,
                                                     @Valid @RequestBody VerificationStatusRequest request,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        var updated = profileService.updateDealerVerification(id, request.status());
        activityClient.record(authorization, "DEALER_PROFILE", id, request.status().name());
        return VerificationProfileResponse.from(updated);
    }
}