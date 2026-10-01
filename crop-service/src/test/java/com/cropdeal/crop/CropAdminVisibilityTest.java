package com.cropdeal.crop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cropdeal.crop.model.Crop;
import com.cropdeal.crop.model.CropStatus;
import com.cropdeal.crop.model.ListingStatus;
import com.cropdeal.crop.repository.CropRepository;
import com.cropdeal.crop.service.CropEventPublisher;
import com.cropdeal.crop.service.CropServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CropAdminVisibilityTest {
    @Mock CropRepository cropRepository;
    @Mock CropEventPublisher eventPublisher;

    private CropServiceImpl cropService;

    @BeforeEach
    void setUp() {
        cropService = new CropServiceImpl(cropRepository, eventPublisher);
    }

    @Test
    void publicListingReadsOnlyActiveListings() {
        Crop crop = crop(CropStatus.AVAILABLE, ListingStatus.ACTIVE);
        when(cropRepository.findByListingStatus(ListingStatus.ACTIVE)).thenReturn(List.of(crop));

        var result = cropService.getAllCrops();

        assertEquals(1, result.size());
        verify(cropRepository).findByListingStatus(ListingStatus.ACTIVE);
    }

    @Test
    void deactivatingListingPreservesCommercialCropStatus() {
        Crop crop = crop(CropStatus.AVAILABLE, ListingStatus.ACTIVE);
        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));
        when(cropRepository.save(crop)).thenReturn(crop);

        var result = cropService.updateListingStatus(1L, ListingStatus.INACTIVE);

        assertEquals(ListingStatus.INACTIVE, result.getListingStatus());
        assertEquals(CropStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void inactiveListingCannotBeReserved() {
        Crop crop = crop(CropStatus.AVAILABLE, ListingStatus.INACTIVE);
        when(cropRepository.findById(1L)).thenReturn(Optional.of(crop));

        assertThrows(IllegalStateException.class, () -> cropService.reserveCrop(1L, 2L, 1.0));
        verify(cropRepository, never()).save(any(Crop.class));
    }

    @Test
    void adminCanInspectInactiveCropWithoutChangingItsCropStatus() {
        Crop crop = crop(CropStatus.SOLD, ListingStatus.INACTIVE);
        when(cropRepository.findById(1L)).thenReturn(java.util.Optional.of(crop));

        var response = cropService.adminGetCrop(1L);

        assertEquals(ListingStatus.INACTIVE, response.getListingStatus());
        assertEquals(CropStatus.SOLD, response.getStatus());
    }

    private Crop crop(CropStatus status, ListingStatus listingStatus) {
        Crop crop = new Crop();
        crop.setId(1L);
        crop.setFarmerId(2L);
        crop.setOwnerEmail("farmer@example.com");
        crop.setCropName("Rice");
        crop.setCropType("Grain");
        crop.setQuantity(10.0);
        crop.setPrice(20.0);
        crop.setLocation("Nashik");
        crop.setStatus(status);
        crop.setListingStatus(listingStatus);
        return crop;
    }
}