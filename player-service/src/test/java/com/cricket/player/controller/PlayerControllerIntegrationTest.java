package com.cricket.player.controller;

import com.cricket.player.dto.CareerStatsUpdateRequest;
import com.cricket.player.dto.PlayerRequest;
import com.cricket.player.entity.PlayerRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class PlayerControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cricket_players_test")
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
    void createPlayer_thenUpdateStats_thenAppearsOnLeaderboard() throws Exception {
        PlayerRequest request = new PlayerRequest(
                "Steve Smith", "Australia", PlayerRole.BATSMAN, "Right-hand bat", null, null);

        String body = mockMvc.perform(post("/api/v1/players")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Steve Smith"))
                .andReturn().getResponse().getContentAsString();

        String playerId = objectMapper.readTree(body).get("id").asText();

        CareerStatsUpdateRequest statsUpdate = new CareerStatsUpdateRequest(120, 100, true, 0, 0, 0);
        mockMvc.perform(patch("/api/v1/players/" + playerId + "/stats")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(statsUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runsScored").value(120))
                .andExpect(jsonPath("$.centuries").value(1));

        mockMvc.perform(get("/api/v1/players/leaderboard/runs?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Steve Smith"));
    }

    @Test
    void getPlayer_returns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/players/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
