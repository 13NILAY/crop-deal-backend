package com.cropdeal.notification.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DealerEmailClient {
    private static final Logger log = LoggerFactory.getLogger(DealerEmailClient.class);
    private final RestClient client;
    private final String internalToken;

    public DealerEmailClient(
            @Value("${profile-service.url:http://localhost:8085}") String profileServiceUrl,
            @Value("${notification.internal-token:CropDealNotificationInternal2026}") String internalToken) {
        this.client = RestClient.builder().baseUrl(profileServiceUrl).build();
        this.internalToken = internalToken;
    }

    public List<String> findAll() {
        try {
            List<String> emails = client.get().uri("/dealers/emails")
                    .header("X-Internal-Token", internalToken)
                    .retrieve().body(new ParameterizedTypeReference<>() {});
            return emails == null ? List.of() : emails;
        } catch (RuntimeException ex) {
            log.warn("Could not load dealer email addresses: {}", ex.getMessage());
            return List.of();
        }
    }
}