package com.cropdeal.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-service")
public interface FarmerProfileClient {
    @GetMapping("/farmers/me")
    FarmerIdentity currentFarmer(@RequestHeader("Authorization") String authorization);

    record FarmerIdentity(Long id, String userEmail, String fullName, String phoneNumber,
                         String address, String location, String verificationStatus) { }
}