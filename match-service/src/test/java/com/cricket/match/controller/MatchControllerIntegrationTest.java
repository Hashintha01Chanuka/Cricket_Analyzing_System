package com.cricket.match.controller;

import com.cricket.match.dto.MatchRequest;
import com.cricket.match.entity.MatchFormat;
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
import java.time.LocalDateTime;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boots the real Spring context against a real, throwaway Postgres container
 * instead of mocks or an in-memory DB — this catches issues that H2 would
 * mask (JSON serialization, actual SQL dialect, real constraints).
 *
 * Role/token enforcement itself (public GET, ADMIN-only POST, expired-token
 * rejection, etc.) is covered separately and more thoroughly in
 * MatchControllerSecurityTest — this class focuses on the CRUD behavior and
 * just carries a valid ADMIN token along for the write calls it needs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class MatchControllerIntegrationTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_matches_test")
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
    void createAndFetchMatch_endToEnd() throws Exception {
        MatchRequest request = new MatchRequest(
                "England", "New Zealand", "Lord's", LocalDateTime.now(), MatchFormat.TEST);

        String responseBody = mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.team1").value("England"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn().getResponse().getContentAsString();

        String matchId = objectMapper.readTree(responseBody).get("id").asText();

        // GET remains public — no token needed here.
        mockMvc.perform(get("/api/v1/matches/" + matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team2").value("New Zealand"));
    }

    @Test
    void createMatch_rejectsInvalidPayload() throws Exception {
        String invalidJson = "{\"team1\": \"\", \"team2\": \"Australia\"}";

        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.team1").exists());
    }

    @Test
    void getMatch_returns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/matches/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    private String adminToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("admin-test-user")
                .claim("role", "ADMIN")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}
