package com.cropdeal.profile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cropdeal.profile.entity.FarmerProfile;
import com.cropdeal.profile.repository.FarmerProfileRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AdminVerificationSecurityTest {
    @Autowired MockMvc mockMvc;
    @Autowired FarmerProfileRepository farmerRepository;
    @Value("${security.jwt.secret}") String jwtSecret;

    @Test
    @Transactional
    @Rollback
    void onlyAdminCanReviewAndUpdateVerification() throws Exception {
        FarmerProfile farmer = new FarmerProfile();
        farmer.setUserEmail("farmer-" + UUID.randomUUID() + "@example.com");
        farmer.setFullName("Test Farmer");
        farmer.setPhoneNumber("1234567890");
        farmer.setAddress("Hidden address");
        farmer.setLocation("Nashik");
        farmer = farmerRepository.saveAndFlush(farmer);

        String url = "/admin/verifications/farmers?status=PENDING";
        mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(url).header("Authorization", bearer("FARMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(url).header("Authorization", bearer("DEALER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(url).header("Authorization", bearer("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].address").doesNotExist());

        mockMvc.perform(patch("/admin/verifications/farmers/{id}", farmer.getId())
                        .header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"VERIFIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    private String bearer(String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject(role.toLowerCase() + "-user@example.com")
                .claim("roles", role).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(key).compact();
        return "Bearer " + token;
    }
}