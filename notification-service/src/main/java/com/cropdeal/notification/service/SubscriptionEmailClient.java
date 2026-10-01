package com.cropdeal.notification.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SubscriptionEmailClient {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionEmailClient.class);
    private final RestClient client;
    private final String internalToken;

    public SubscriptionEmailClient(
            @Value("${subscription-service.url:http://localhost:8090}") String serviceUrl,
            @Value("${subscription.internal-token:CropDealSubscriptionInternal2026}") String internalToken) {
        this.client = RestClient.builder().baseUrl(serviceUrl).build();
        this.internalToken = internalToken;
    }

    public List<String> findForFarmer(Long farmerId) {
        try {
            List<String> emails = client.get().uri("/subscriptions/farmers/{farmerId}/dealers", farmerId)
                    .header("X-Internal-Token", internalToken)
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            return emails == null ? List.of() : emails;
        } catch (RuntimeException ex) {
            log.warn("Could not load subscribed dealer emails for farmer {}: {}", farmerId, ex.getMessage());
            return List.of();
        }
    }
}
