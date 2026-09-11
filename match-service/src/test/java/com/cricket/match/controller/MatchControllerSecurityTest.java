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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the security rules with real, signed JWTs rather than mocking the
 * SecurityContext — this is what actually catches a wrong role string or a
 * misconfigured requestMatcher. The secret used to sign these test tokens
 * matches application.properties' local-dev default exactly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class MatchControllerSecurityTest {

    private static final String TEST_SECRET = "this-is-a-local-dev-only-secret-key-change-me-32chars+";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_matches_security_test")
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
    void listMatches_publicWithNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isOk());
    }

    @Test
    void createMatch_rejectedWithNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/matches")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleMatchRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMatch_rejectedForScorerRole() throws Exception {
        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", "Bearer " + tokenFor("scorer1", "SCORER"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleMatchRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMatch_allowedForAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", "Bearer " + tokenFor("admin1", "ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleMatchRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void createMatch_rejectedForExpiredToken() throws Exception {
        mockMvc.perform(post("/api/v1/matches")
                        .header("Authorization", "Bearer " + expiredTokenFor("admin1", "ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleMatchRequest())))
                .andExpect(status().isUnauthorized());
    }

    private MatchRequest sampleMatchRequest() {
        return new MatchRequest("India", "England", "Eden Gardens", LocalDateTime.now(), MatchFormat.ODI);
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

    private String expiredTokenFor(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Instant past = Instant.now().minusSeconds(7200);
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(past))
                .expiration(Date.from(past.plusSeconds(3600))) // expired an hour ago
                .signWith(key)
                .compact();
    }
}
