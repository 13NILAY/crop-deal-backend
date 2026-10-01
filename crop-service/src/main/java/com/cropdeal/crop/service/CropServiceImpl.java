package com.cropdeal.crop.service;
 
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
 
import com.cropdeal.crop.dto.CropRequestDTO;
import com.cropdeal.crop.dto.CropResponseDTO;
import com.cropdeal.crop.event.CropListedEvent;
import com.cropdeal.crop.exception.CropNotFoundException;
import com.cropdeal.crop.model.Crop;
import com.cropdeal.crop.model.CropStatus;
import com.cropdeal.crop.model.ListingStatus;
import com.cropdeal.crop.repository.CropRepository;
 
@Service
public class CropServiceImpl implements CropService {
 
    private final CropRepository cropRepository;
    private final CropEventPublisher cropEventPublisher;
 
    public CropServiceImpl(CropRepository cropRepository, CropEventPublisher cropEventPublisher) {
        this.cropRepository = cropRepository;
        this.cropEventPublisher = cropEventPublisher;
    }
 
    @Override
    public CropResponseDTO createCrop(String ownerEmail, CropRequestDTO request) {
 
        Crop crop = new Crop();
 
        crop.setFarmerId(request.getFarmerId());
        crop.setOwnerEmail(ownerEmail);
        crop.setCropName(request.getCropName());
        crop.setCropType(request.getCropType());
        crop.setQuantity(request.getQuantity());
        crop.setPrice(request.getPrice());
        crop.setLocation(request.getLocation());
 
        // New crops are available by default
        crop.setStatus(CropStatus.AVAILABLE);
        crop.setListingStatus(ListingStatus.ACTIVE);
 
        Crop savedCrop = cropRepository.save(crop);
        publishCropEvent(savedCrop, "LISTED");
 
        return convertToResponseDTO(savedCrop);
    }
 
    @Override
    public CropResponseDTO getCropById(Long id) {
 
        Crop crop = cropRepository.findByIdAndListingStatus(id, ListingStatus.ACTIVE)
                .orElseThrow(() ->
                        new CropNotFoundException(
                                "Crop not found with id: " + id));
 
        return convertToResponseDTO(crop);
    }
 
    @Override
    public List<CropResponseDTO> getAllCrops() {
 
        return cropRepository.findByListingStatus(ListingStatus.ACTIVE)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CropResponseDTO> getMyCrops(String ownerEmail) {
        return cropRepository.findByOwnerEmailIgnoreCase(ownerEmail).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public List<CropResponseDTO> searchByCropName(String cropName) {
 
        return cropRepository
            .findByListingStatusAndCropNameContainingIgnoreCase(ListingStatus.ACTIVE, cropName)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public List<CropResponseDTO> filterByCropType(String cropType) {
 
        return cropRepository
            .findByListingStatusAndCropTypeContainingIgnoreCase(ListingStatus.ACTIVE, cropType)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public List<CropResponseDTO> searchByLocation(String location) {
 
        return cropRepository
            .findByListingStatusAndLocationContainingIgnoreCase(ListingStatus.ACTIVE, location)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public Page<CropResponseDTO> getCropsWithPagination(
            int page,
            int size,
            String sortBy,
            String direction) {
 
        Sort sort;
 
        if (direction.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }
 
        Pageable pageable = PageRequest.of(page, size, sort);
 
        return cropRepository.findByListingStatus(ListingStatus.ACTIVE, pageable)
                .map(this::convertToResponseDTO);
    }
 
    @Override
    public CropResponseDTO updateCrop(
            Long id,
            String ownerEmail,
            CropRequestDTO request) {
 
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() ->
                        new CropNotFoundException(
                                "Crop not found with id: " + id));

            verifyOwner(crop, ownerEmail);
 
        crop.setFarmerId(request.getFarmerId());
        crop.setCropName(request.getCropName());
        crop.setCropType(request.getCropType());
        crop.setQuantity(request.getQuantity());
        crop.setPrice(request.getPrice());
        crop.setLocation(request.getLocation());
 
        // Status is intentionally not changed here
 
        Crop updatedCrop = cropRepository.save(crop);
        publishCropEvent(updatedCrop, "UPDATED");
 
        return convertToResponseDTO(updatedCrop);
    }
 
    @Override
    public void deleteCrop(Long id, String ownerEmail) {
 
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() ->
                        new CropNotFoundException(
                                "Crop not found with id: " + id));

        verifyOwner(crop, ownerEmail);
 
        cropRepository.delete(crop);
    }

    private void verifyOwner(Crop crop, String ownerEmail) {
        if (!ownerEmail.equalsIgnoreCase(crop.getOwnerEmail())) {
            throw new org.springframework.security.access.AccessDeniedException("Crop belongs to another farmer");
        }
    }

    @Override
    @Transactional
    public synchronized CropResponseDTO reserveCrop(Long id, Long farmerId, Double quantity) {
        Crop crop = cropRepository.findById(id).orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));
        if (!farmerId.equals(crop.getFarmerId()) || crop.getListingStatus() != ListingStatus.ACTIVE
            || crop.getStatus() != CropStatus.AVAILABLE
                || crop.getQuantity() < quantity) {
            throw new IllegalStateException("Crop is unavailable or has insufficient quantity");
        }
        crop.setQuantity(crop.getQuantity() - quantity);
        if (crop.getQuantity() == 0) crop.setStatus(CropStatus.SOLD);
        return convertToResponseDTO(cropRepository.save(crop));
    }

    @Override
    public Page<CropResponseDTO> adminSearch(ListingStatus listingStatus, CropStatus cropStatus,
                                              String search, Long farmerId, Pageable pageable) {
        Specification<Crop> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (listingStatus != null) predicates.add(criteriaBuilder.equal(root.get("listingStatus"), listingStatus));
            if (cropStatus != null) predicates.add(criteriaBuilder.equal(root.get("status"), cropStatus));
            if (farmerId != null) predicates.add(criteriaBuilder.equal(root.get("farmerId"), farmerId));
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("cropName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), term)));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Pageable boundedPageable = PageRequest.of(Math.max(0, pageable.getPageNumber()),
                Math.max(1, Math.min(pageable.getPageSize(), 100)), pageable.getSort());
        return cropRepository.findAll(specification, boundedPageable).map(this::convertToResponseDTO);
    }

    @Override
    public CropResponseDTO adminGetCrop(Long id) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));
        return convertToResponseDTO(crop);
    }

    @Override
    public CropResponseDTO updateListingStatus(Long id, ListingStatus listingStatus) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new CropNotFoundException("Crop not found with id: " + id));
        crop.setListingStatus(listingStatus);
        return convertToResponseDTO(cropRepository.save(crop));
    }
 
    private CropResponseDTO convertToResponseDTO(Crop crop) {
 
        CropResponseDTO response = new CropResponseDTO();
 
        response.setId(crop.getId());
        response.setFarmerId(crop.getFarmerId());
        response.setCropName(crop.getCropName());
        response.setCropType(crop.getCropType());
        response.setQuantity(crop.getQuantity());
        response.setPrice(crop.getPrice());
        response.setLocation(crop.getLocation());
        response.setStatus(crop.getStatus());
        response.setListingStatus(crop.getListingStatus());
        response.setCreatedAt(crop.getCreatedAt());
 
        return response;
    }

    private void publishCropEvent(Crop crop, String action) {
        cropEventPublisher.publish(new CropListedEvent(
            crop.getId(), crop.getFarmerId(), crop.getOwnerEmail(), crop.getCropName(), crop.getCropType(),
                crop.getQuantity(), crop.getPrice(), crop.getLocation(), action));
    }
}
 