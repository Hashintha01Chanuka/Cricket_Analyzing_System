package com.cricket.stats.controller;

import com.cricket.stats.dto.MatchResultRequest;
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
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Only covers the parts of Stats Service that don't require a live Match
 * Service (recording results + head-to-head). The projected-score endpoint,
 * which calls out to Match Service over REST, is intentionally left to
 * manual/Postman testing for now — properly integration-testing that call
 * needs a stubbed HTTP server (e.g. WireMock), which is a good addition
 * once Docker Compose wires the real services together.
 *
 * Role enforcement itself is covered more thoroughly in
 * StatsControllerSecurityTest; this class carries a valid ADMIN token for
 * the write call it needs (recording results) and deliberately sends none
 * for win-probability, which is public.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class StatsControllerIntegrationTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_stats_test")
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
    void recordResults_thenHeadToHeadReflectsTally() throws Exception {
        MatchResultRequest first = new MatchResultRequest(null, "India", "Pakistan", "India",
                "6 wickets", LocalDate.of(2024, 1, 10));
        MatchResultRequest second = new MatchResultRequest(null, "Pakistan", "India", "Pakistan",
                "20 runs", LocalDate.of(2024, 2, 15));

        mockMvc.perform(post("/api/v1/stats/results")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json").content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/stats/results")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json").content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());

        // GET remains public.
        mockMvc.perform(get("/api/v1/stats/head-to-head")
                        .param("team1", "India").param("team2", "Pakistan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatches").value(2))
                .andExpect(jsonPath("$.team1Wins").value(1))
                .andExpect(jsonPath("$.team2Wins").value(1));
    }

    @Test
    void winProbability_rejectsInvalidWicketsLost() throws Exception {
        // No Authorization header at all — win-probability is intentionally
        // public since it's a stateless calculator, not a write.
        String invalidJson = "{\"target\":150,\"currentScore\":100,\"wicketsLost\":15,\"oversRemaining\":5,\"totalOvers\":20}";

        mockMvc.perform(post("/api/v1/stats/win-probability")
                        .contentType("application/json").content(invalidJson))
                .andExpect(status().isBadRequest());
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
