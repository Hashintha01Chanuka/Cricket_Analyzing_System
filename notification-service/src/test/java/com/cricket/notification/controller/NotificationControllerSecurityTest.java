package com.cricket.notification.controller;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class NotificationControllerSecurityTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_notifications_security_test")
            .withUsername("cricket")
            .withPassword("cricket");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getByMatch_publicWithNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/match/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    void checkMilestone_rejectedWithNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/check-milestone")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkMilestone_rejectedForViewerRole() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/check-milestone")
                        .header("Authorization", "Bearer " + tokenFor("viewer1", "VIEWER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkMilestone_allowedForScorerRole() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/check-milestone")
                        .header("Authorization", "Bearer " + tokenFor("scorer1", "SCORER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void markRead_rejectedWithNoToken() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/" + UUID.randomUUID() + "/read"))
                .andExpect(status().isUnauthorized());
    }

    private MilestoneCheckRequest sampleRequest() {
        return new MilestoneCheckRequest(UUID.randomUUID(), UUID.randomUUID(), "Player",
                10, 12, false, null, null, 0);
    }

    private String tokenFor(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}
