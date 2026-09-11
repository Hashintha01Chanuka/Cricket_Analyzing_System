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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Role enforcement is covered separately in NotificationControllerSecurityTest;
 * this class carries a valid SCORER token for the check-milestone calls it needs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class NotificationControllerIntegrationTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_notifications_test")
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
    void checkMilestone_crossingFifty_createsNotificationVisibleByMatchAndPlayer() throws Exception {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        MilestoneCheckRequest request = new MilestoneCheckRequest(matchId, playerId, "Babar Azam",
                48, 54, false, null, null, 0);

        mockMvc.perform(post("/api/v1/notifications/check-milestone")
                        .header("Authorization", "Bearer " + scorerToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("FIFTY"));

        // GET remains public.
        mockMvc.perform(get("/api/v1/notifications/match/" + matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value(org.hamcrest.Matchers.containsString("Babar Azam")));

        mockMvc.perform(get("/api/v1/notifications/player/" + playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkMilestone_noThresholdCrossed_returnsEmptyList() throws Exception {
        MilestoneCheckRequest request = new MilestoneCheckRequest(UUID.randomUUID(), UUID.randomUUID(),
                "Player", 10, 14, false, null, null, 0);

        mockMvc.perform(post("/api/v1/notifications/check-milestone")
                        .header("Authorization", "Bearer " + scorerToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String scorerToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("scorer-test-user")
                .claim("role", "SCORER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}
