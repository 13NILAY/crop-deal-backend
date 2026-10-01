package com.cropdeal.payment.service;

import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AdminActivityClient {
    private static final Logger logger = LoggerFactory.getLogger(AdminActivityClient.class);
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient = RestClient.create();

    public AdminActivityClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public void record(String authorization, String action, Long payoutId) {
        try {
            ServiceInstance authService = discoveryClient.getInstances("AUTHENTICATION-SERVICE").stream()
                    .findFirst().orElseThrow();
            restClient.post().uri(URI.create(authService.getUri() + "/internal/admin/activity"))
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("action", action, "entityType", "PAYOUT", "entityId", payoutId.toString()))
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException exception) {
            logger.warn("Could not record Admin payout activity");
        }
    }
}