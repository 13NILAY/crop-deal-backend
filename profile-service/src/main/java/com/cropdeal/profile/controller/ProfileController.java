package com.cropdeal.profile.controller;

import com.cropdeal.profile.dto.*;
import com.cropdeal.profile.entity.*;
import com.cropdeal.profile.service.ProfileService;
import jakarta.validation.Valid;
import java.util.List;
import java.security.Principal;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final ProfileService service;
    private final String internalToken;

    public ProfileController(ProfileService service,
            @Value("${notification.internal-token}") String internalToken) {
        this.service = service;
        this.internalToken = internalToken;
    }

    @PostMapping("/farmers")
    public ResponseEntity<FarmerProfile> createFarmer(@Valid @RequestBody FarmerRequest r, Principal p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFarmer(p.getName(), r));
    }

    @GetMapping("/farmers/me")
    public FarmerProfile currentFarmer(Principal p) {
        return service.farmer(p.getName());
    }

    @GetMapping("/farmers/directory")
    public List<FarmerDirectoryResponse> farmerDirectory() {
        return service.farmers().stream().map(FarmerDirectoryResponse::from).toList();
    }

    @GetMapping("/farmers")
    public List<FarmerProfile> farmers() {
        return service.farmers();
    }

    @GetMapping("/farmers/{id}")
    public FarmerProfile farmer(@PathVariable Long id) {
        return service.farmer(id);
    }

    @PutMapping("/farmers/{id}")
    public FarmerProfile updateFarmer(@PathVariable Long id, @Valid @RequestBody FarmerRequest r, Principal p) {
        return service.updateFarmer(id, p.getName(), r);
    }

    @DeleteMapping("/farmers/{id}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long id, Principal p) {
        service.deleteFarmer(id, p.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dealers")
    public ResponseEntity<DealerProfile> createDealer(@Valid @RequestBody DealerRequest r, Principal p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDealer(p.getName(), r));
    }

    @GetMapping("/dealers/me")
    public DealerProfile currentDealer(Principal p) {
        return service.dealer(p.getName());
    }

    @GetMapping("/dealers/{id}")
    public DealerProfile dealer(@PathVariable Long id) {
        return service.dealer(id);
    }

    @GetMapping("/dealers/emails")
    public List<String> dealerEmails(@RequestHeader("X-Internal-Token") String token) {
        if (!internalToken.equals(token)) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid internal token");
        }
        return service.dealers().stream().map(DealerProfile::getUserEmail).toList();
    }

    @PutMapping("/dealers/{id}")
    public DealerProfile updateDealer(@PathVariable Long id, @Valid @RequestBody DealerRequest r, Principal p) {
        return service.updateDealer(id, p.getName(), r);
    }

    @DeleteMapping("/dealers/{id}")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id, Principal p) {
        service.deleteDealer(id, p.getName());
        return ResponseEntity.noContent().build();
    }
}
