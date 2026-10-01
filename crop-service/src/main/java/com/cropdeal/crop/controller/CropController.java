package com.cropdeal.crop.controller;
 
import java.util.List;
 
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
 
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
 
import com.cropdeal.crop.dto.CropRequestDTO;
import com.cropdeal.crop.dto.CropResponseDTO;
import com.cropdeal.crop.service.CropService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
 
@RestController
@RequestMapping("/crops")
@SecurityRequirement(name = "bearerAuth")
public class CropController {
 
    private final CropService cropService;
 
    public CropController(CropService cropService) {
        this.cropService = cropService;
    }
 
    @PostMapping
    public ResponseEntity<CropResponseDTO> createCrop(
                        @Valid @RequestBody CropRequestDTO request, Principal principal) {
 
        CropResponseDTO response =
                                cropService.createCrop(principal.getName(), request);
 
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
 
    @GetMapping
    public ResponseEntity<List<CropResponseDTO>> getAllCrops() {
 
        return ResponseEntity.ok(
                cropService.getAllCrops());
    }

        @GetMapping("/mine")
        public ResponseEntity<List<CropResponseDTO>> getMyCrops(Principal principal) {
                return ResponseEntity.ok(cropService.getMyCrops(principal.getName()));
        }
 
    @GetMapping("/{id}")
    public ResponseEntity<CropResponseDTO> getCropById(
            @PathVariable Long id) {
 
        return ResponseEntity.ok(
                cropService.getCropById(id));
    }
 
    @GetMapping("/search")
    public ResponseEntity<List<CropResponseDTO>> searchByName(
            @RequestParam String name) {
 
        return ResponseEntity.ok(
                cropService.searchByCropName(name));
    }
 
    @GetMapping("/filter/type")
    public ResponseEntity<List<CropResponseDTO>> filterByType(
            @RequestParam String type) {
 
        return ResponseEntity.ok(
                cropService.filterByCropType(type));
    }
 
    @GetMapping("/search/location")
    public ResponseEntity<List<CropResponseDTO>> searchByLocation(
            @RequestParam String location) {
 
        return ResponseEntity.ok(
                cropService.searchByLocation(location));
    }
 
    @GetMapping("/page")
    public ResponseEntity<Page<CropResponseDTO>>
            getCropsWithPagination(
 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
 
        return ResponseEntity.ok(
                cropService.getCropsWithPagination(
                        page,
                        size,
                        sortBy,
                        direction));
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<CropResponseDTO> updateCrop(
            @PathVariable Long id,
                        @Valid @RequestBody CropRequestDTO request, Principal principal) {
 
        return ResponseEntity.ok(
                                cropService.updateCrop(id, principal.getName(), request));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrop(
                        @PathVariable Long id, Principal principal) {
 
                cropService.deleteCrop(id, principal.getName());
 
        return ResponseEntity.noContent().build();
    }

        @PostMapping("/{id}/reserve")
        public ResponseEntity<CropResponseDTO> reserve(@PathVariable Long id,
                        @RequestParam @Positive Long farmerId, @RequestParam @Positive Double quantity) {
                return ResponseEntity.ok(cropService.reserveCrop(id, farmerId, quantity));
        }
}
 