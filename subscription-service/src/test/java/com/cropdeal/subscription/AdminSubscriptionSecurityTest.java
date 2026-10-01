package com.cropdeal.subscription;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cropdeal.subscription.entity.CropSubscription;
import com.cropdeal.subscription.repository.CropSubscriptionRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSubscriptionSecurityTest {
    @Autowired MockMvc mockMvc;
    @Autowired CropSubscriptionRepository repository;
    @Value("${security.jwt.secret}") String jwtSecret;

    @Test
    @Transactional
    @Rollback
    void onlyAdminCanViewSubscriptions() throws Exception {
        String dealerEmail = "dealer-" + UUID.randomUUID() + "@example.com";
        CropSubscription subscription = new CropSubscription();
        subscription.setFarmerId(71L);
        subscription.setDealerEmail(dealerEmail);
        subscription.setCreatedAt(LocalDateTime.now());
        repository.saveAndFlush(subscription);

        String adminToken = token("admin@example.com", "ADMIN");
        String farmerToken = token("farmer@example.com", "FARMER");
        String dealerToken = token("dealer@example.com", "DEALER");
        String url = "/admin/subscriptions?farmerId=71&dealerEmail=" + dealerEmail;

        mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(url).header("Authorization", "Bearer " + farmerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(url).header("Authorization", "Bearer " + dealerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(url).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dealerEmail").value(dealerEmail))
                .andExpect(jsonPath("$.content[0].farmerId").value(71));
    }

    private String token(String subject, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().subject(subject).claim("roles", role).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(key).compact();
    }
}