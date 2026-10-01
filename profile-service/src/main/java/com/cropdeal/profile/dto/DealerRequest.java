package com.cropdeal.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DealerRequest(@NotBlank @Size(max = 150) String businessName, @NotBlank @Size(max = 150) String contactName,
        @NotBlank @Pattern(regexp = "^[0-9+()\\- ]{7,30}$") String phoneNumber, @NotBlank @Size(max = 500) String address,
        @NotBlank @Size(max = 150) String location) {
}
