package com.cropdeal.order.client;

import com.cropdeal.order.dto.CropResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "crop-service")
public interface CropClient {

    @PostMapping("/crops/{id}/reserve")
    CropResponse reserve(
            @PathVariable("id") Long cropId,
            @RequestParam("farmerId") Long farmerId,
            @RequestParam("quantity") java.math.BigDecimal quantity,
            @RequestHeader("Authorization") String authorization);
}