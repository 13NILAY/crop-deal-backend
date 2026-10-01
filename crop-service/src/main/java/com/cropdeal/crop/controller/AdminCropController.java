package com.cropdeal.crop.controller;

import com.cropdeal.crop.dto.CropResponseDTO;
import com.cropdeal.crop.dto.UpdateListingStatusRequest;
import com.cropdeal.crop.model.CropStatus;
import com.cropdeal.crop.model.ListingStatus;
import com.cropdeal.crop.service.CropService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/crops")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCropController {
    private final CropService cropService;
    private final com.cropdeal.crop.service.AdminActivityClient activityClient;

    public AdminCropController(CropService cropService, com.cropdeal.crop.service.AdminActivityClient activityClient) {
        this.cropService = cropService;
        this.activityClient = activityClient;
    }

    @GetMapping
    public Page<CropResponseDTO> list(@RequestParam(required = false) ListingStatus status,
                                      @RequestParam(required = false) CropStatus cropStatus,
                                      @RequestParam(required = false) String search,
                                      @RequestParam(required = false) Long farmerId,
                                      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                      Pageable pageable) {
        return cropService.adminSearch(status, cropStatus, search, farmerId, pageable);
    }

    @GetMapping("/{id}")
    public CropResponseDTO get(@PathVariable Long id) {
        return cropService.adminGetCrop(id);
    }

    @PatchMapping("/{id}/status")
    public CropResponseDTO updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateListingStatusRequest request,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        CropResponseDTO updated = cropService.updateListingStatus(id, request.status());
        String action = request.status() == ListingStatus.INACTIVE ? "ADMIN_CROP_DISABLED" : "ADMIN_CROP_REACTIVATED";
        activityClient.record(authorization, action, id);
        return updated;
    }
}