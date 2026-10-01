package com.cropdeal.profile.service;

import com.cropdeal.profile.dto.*;
import com.cropdeal.profile.entity.*;
import com.cropdeal.profile.repository.*;
import java.util.List;
import java.util.NoSuchElementException;
import com.cropdeal.profile.entity.VerificationStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final FarmerProfileRepository farmers;
    private final DealerProfileRepository dealers;

    public ProfileService(FarmerProfileRepository farmers, DealerProfileRepository dealers) {
        this.farmers = farmers;
        this.dealers = dealers;
    }

    public FarmerProfile farmer(Long id) {
        return farmers.findById(id).orElseThrow(() -> new NoSuchElementException("Farmer profile not found"));
    }

    public FarmerProfile farmer(String email) {
        return farmers.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer profile not found"));
    }

    public List<FarmerProfile> farmers() {
        return farmers.findAll();
    }

    public DealerProfile dealer(Long id) {
        return dealers.findById(id).orElseThrow(() -> new NoSuchElementException("Dealer profile not found"));
    }

    public DealerProfile dealer(String email) {
        return dealers.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dealer profile not found"));
    }

    public List<DealerProfile> dealers() {
        return dealers.findAll();
    }

    public List<FarmerProfile> farmersByVerification(VerificationStatus status) {
        return farmers.findByVerificationStatus(status);
    }

    public List<DealerProfile> dealersByVerification(VerificationStatus status) {
        return dealers.findByVerificationStatus(status);
    }

    public FarmerProfile updateFarmerVerification(Long id, VerificationStatus status) {
        FarmerProfile profile = farmer(id);
        profile.setVerificationStatus(status);
        return farmers.save(profile);
    }

    public DealerProfile updateDealerVerification(Long id, VerificationStatus status) {
        DealerProfile profile = dealer(id);
        profile.setVerificationStatus(status);
        return dealers.save(profile);
    }

    public FarmerProfile createFarmer(String email, FarmerRequest r) {
        FarmerProfile p = new FarmerProfile();
        p.setUserEmail(email);
        apply(p, r);
        return farmers.save(p);
    }

    public FarmerProfile updateFarmer(Long id, String email, FarmerRequest r) {
        FarmerProfile p = ownedFarmer(id, email);
        apply(p, r);
        return farmers.save(p);
    }

    public void deleteFarmer(Long id, String email) {
        farmers.delete(ownedFarmer(id, email));
    }

    public DealerProfile createDealer(String email, DealerRequest r) {
        DealerProfile p = new DealerProfile();
        p.setUserEmail(email);
        apply(p, r);
        return dealers.save(p);
    }

    public DealerProfile updateDealer(Long id, String email, DealerRequest r) {
        DealerProfile p = ownedDealer(id, email);
        apply(p, r);
        return dealers.save(p);
    }

    public void deleteDealer(Long id, String email) {
        dealers.delete(ownedDealer(id, email));
    }

    private FarmerProfile ownedFarmer(Long id, String email) {
        FarmerProfile p = farmer(id);
        if (!p.getUserEmail().equals(email))
            throw new SecurityException("Profile belongs to another user");
        return p;
    }

    private DealerProfile ownedDealer(Long id, String email) {
        DealerProfile p = dealer(id);
        if (!p.getUserEmail().equals(email))
            throw new SecurityException("Profile belongs to another user");
        return p;
    }

    private void apply(FarmerProfile p, FarmerRequest r) {
        p.setFullName(r.fullName());
        p.setPhoneNumber(r.phoneNumber());
        p.setAddress(r.address());
        p.setLocation(r.location());
    }

    private void apply(DealerProfile p, DealerRequest r) {
        p.setBusinessName(r.businessName());
        p.setContactName(r.contactName());
        p.setPhoneNumber(r.phoneNumber());
        p.setAddress(r.address());
        p.setLocation(r.location());
    }
}
