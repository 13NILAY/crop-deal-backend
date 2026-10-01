package com.cropdeal.profile.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cropdeal.profile.dto.FarmerRequest;
import com.cropdeal.profile.dto.DealerRequest;
import com.cropdeal.profile.entity.DealerProfile;
import com.cropdeal.profile.entity.FarmerProfile;
import com.cropdeal.profile.entity.VerificationStatus;
import com.cropdeal.profile.repository.DealerProfileRepository;
import com.cropdeal.profile.repository.FarmerProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceUnitTest {
    @Mock FarmerProfileRepository farmers;
    @Mock DealerProfileRepository dealers;
    @InjectMocks ProfileService service;

    @Test
    void createsFarmerProfileForAuthenticatedEmail() {
        FarmerRequest request = new FarmerRequest("Ravi Kumar", "9876543210", "Farm Road", "Pune");
        when(farmers.save(any(FarmerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FarmerProfile profile = service.createFarmer("ravi@example.com", request);

        assertEquals("ravi@example.com", profile.getUserEmail());
        assertEquals("Ravi Kumar", profile.getFullName());
        verify(farmers).save(any(FarmerProfile.class));
    }

    @Test
    void findsFarmerProfileForAuthenticatedEmail() {
        FarmerProfile profile = new FarmerProfile();
        profile.setUserEmail("ravi@example.com");
        when(farmers.findByUserEmail("ravi@example.com")).thenReturn(Optional.of(profile));

        assertSame(profile, service.farmer("ravi@example.com"));
    }

    @Test
    void rejectsUpdateForDifferentUser() {
        FarmerProfile profile = new FarmerProfile();
        profile.setUserEmail("owner@example.com");
        when(farmers.findById(1L)).thenReturn(Optional.of(profile));

        assertThrows(SecurityException.class, () -> service.updateFarmer(1L, "other@example.com",
                new FarmerRequest("Name", "9876543210", "Address", "Location")));
        verify(farmers, never()).save(any(FarmerProfile.class));
    }

    @Test
    void farmerVerificationDefaultsToPendingAndCanBeUpdated() {
        FarmerProfile profile = new FarmerProfile();
        profile.setUserEmail("farmer@example.com");
        when(farmers.findById(8L)).thenReturn(Optional.of(profile));
        when(farmers.save(profile)).thenReturn(profile);

        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus());
        FarmerProfile updated = service.updateFarmerVerification(8L, VerificationStatus.VERIFIED);

        assertEquals(VerificationStatus.VERIFIED, updated.getVerificationStatus());
        verify(farmers).save(profile);
    }

    @Test
    void createsDealerProfileWithAuthenticatedEmail() {
        DealerRequest request = new DealerRequest("Crop Shop", "Devi", "9876543210", "Market Road", "Pune");
        when(dealers.save(any(DealerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealerProfile profile = service.createDealer("dealer@example.com", request);

        assertEquals("dealer@example.com", profile.getUserEmail());
        assertEquals("Crop Shop", profile.getBusinessName());
        assertEquals(VerificationStatus.PENDING, profile.getVerificationStatus());
    }
}