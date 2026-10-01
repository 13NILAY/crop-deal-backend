package com.cropdeal.crop.repository;
 
import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.cropdeal.crop.model.Crop;
import com.cropdeal.crop.model.ListingStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
 
public interface CropRepository extends JpaRepository<Crop, Long>, JpaSpecificationExecutor<Crop> {

    List<Crop> findByListingStatus(ListingStatus listingStatus);

    List<Crop> findByOwnerEmailIgnoreCase(String ownerEmail);

    Optional<Crop> findByIdAndListingStatus(Long id, ListingStatus listingStatus);

    List<Crop> findByListingStatusAndCropNameContainingIgnoreCase(ListingStatus listingStatus, String cropName);

    List<Crop> findByListingStatusAndCropTypeContainingIgnoreCase(ListingStatus listingStatus, String cropType);

    List<Crop> findByListingStatusAndLocationContainingIgnoreCase(ListingStatus listingStatus, String location);

    Page<Crop> findByListingStatus(ListingStatus listingStatus, Pageable pageable);

    long countByListingStatus(ListingStatus listingStatus);

    long countByListingStatusAndStatus(ListingStatus listingStatus, com.cropdeal.crop.model.CropStatus status);
 
    List<Crop> findByCropNameContainingIgnoreCase(String cropName);
 
    List<Crop> findByCropTypeContainingIgnoreCase(String cropType);
 
    List<Crop> findByLocationContainingIgnoreCase(String location);
 
    Page<Crop> findAll(Pageable pageable);
}
 