package com.cropdeal.crop.service;
 
import java.util.List;
 
import org.springframework.data.domain.Page;
 
import com.cropdeal.crop.dto.CropRequestDTO;
import com.cropdeal.crop.dto.CropResponseDTO;
import com.cropdeal.crop.model.CropStatus;
import com.cropdeal.crop.model.ListingStatus;
import org.springframework.data.domain.Pageable;
 
public interface CropService {
 
        CropResponseDTO createCrop(String ownerEmail, CropRequestDTO request);
 
    CropResponseDTO getCropById(Long id);
 
    List<CropResponseDTO> getAllCrops();

        List<CropResponseDTO> getMyCrops(String ownerEmail);
 
    List<CropResponseDTO> searchByCropName(String cropName);
 
    List<CropResponseDTO> filterByCropType(String cropType);
 
    List<CropResponseDTO> searchByLocation(String location);
 
    Page<CropResponseDTO> getCropsWithPagination(
            int page,
            int size,
            String sortBy,
            String direction);
 
    CropResponseDTO updateCrop(
            Long id,
            String ownerEmail,
            CropRequestDTO request);
 
    void deleteCrop(Long id, String ownerEmail);

        CropResponseDTO reserveCrop(Long id, Long farmerId, Double quantity);

        Page<CropResponseDTO> adminSearch(ListingStatus listingStatus, CropStatus cropStatus,
                                          String search, Long farmerId, Pageable pageable);

        CropResponseDTO adminGetCrop(Long id);

        CropResponseDTO updateListingStatus(Long id, ListingStatus listingStatus);
}
 