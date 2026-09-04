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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class StatsControllerSecurityTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_stats_security_test")
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
    void headToHead_publicWithNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/stats/head-to-head?team1=A&team2=B"))
                .andExpect(status().isOk());
    }

    @Test
    void winProbability_publicWithNoToken() throws Exception {
        String validJson = "{\"target\":150,\"currentScore\":100,\"wicketsLost\":3,\"oversRemaining\":5,\"totalOvers\":20}";

        mockMvc.perform(post("/api/v1/stats/win-probability")
                        .contentType("application/json").content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void recordResult_rejectedWithNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/stats/results")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleResult())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recordResult_rejectedForScorerRole() throws Exception {
        mockMvc.perform(post("/api/v1/stats/results")
                        .header("Authorization", "Bearer " + tokenFor("scorer1", "SCORER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleResult())))
                .andExpect(status().isForbidden());
    }

    @Test
    void recordResult_allowedForAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/stats/results")
                        .header("Authorization", "Bearer " + tokenFor("admin1", "ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleResult())))
                .andExpect(status().isCreated());
    }

    private MatchResultRequest sampleResult() {
        return new MatchResultRequest(null, "India", "Sri Lanka", "India", "5 wickets", LocalDate.of(2024, 3, 1));
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
