package com.cricket.player.controller;

import com.cricket.player.dto.CareerStatsUpdateRequest;
import com.cricket.player.dto.PlayerRequest;
import com.cricket.player.entity.PlayerRole;
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
class PlayerControllerSecurityTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_players_security_test")
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
    void listPlayers_publicWithNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/players")).andExpect(status().isOk());
    }

    @Test
    void createPlayer_rejectedWithNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(samplePlayerRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPlayer_rejectedForScorerRole() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + tokenFor("scorer1", "SCORER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(samplePlayerRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPlayer_allowedForAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + tokenFor("admin1", "ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(samplePlayerRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void updateStats_allowedForScorerRole() throws Exception {
        // A SCORER can create... no — SCORER can't create players (ADMIN-only),
        // so we create with ADMIN first, then confirm SCORER can update stats.
        String body = mockMvc.perform(post("/api/v1/players")
                        .header("Authorization", "Bearer " + tokenFor("admin1", "ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(samplePlayerRequest())))
                .andReturn().getResponse().getContentAsString();
        String playerId = objectMapper.readTree(body).get("id").asText();

        CareerStatsUpdateRequest statsUpdate = new CareerStatsUpdateRequest(30, 25, false, 0, 0, 0);
        mockMvc.perform(patch("/api/v1/players/" + playerId + "/stats")
                        .header("Authorization", "Bearer " + tokenFor("scorer1", "SCORER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(statsUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStats_rejectedForViewerRole() throws Exception {
        UUID randomPlayerId = UUID.randomUUID();
        CareerStatsUpdateRequest statsUpdate = new CareerStatsUpdateRequest(30, 25, false, 0, 0, 0);

        mockMvc.perform(patch("/api/v1/players/" + randomPlayerId + "/stats")
                        .header("Authorization", "Bearer " + tokenFor("viewer1", "VIEWER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(statsUpdate)))
                .andExpect(status().isForbidden());
    }

    private PlayerRequest samplePlayerRequest() {
        return new PlayerRequest("Test Player", "India", PlayerRole.BATSMAN, "Right-hand bat", null, null);
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
