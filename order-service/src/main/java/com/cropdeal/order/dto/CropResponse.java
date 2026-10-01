package com.cropdeal.order.dto;

import java.math.BigDecimal;

public record CropResponse(Long id, Long farmerId, BigDecimal quantity, BigDecimal price, String status) { }