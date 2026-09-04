package com.cricket.player.controller;

import com.cricket.player.dto.*;
import com.cricket.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(request));
    }

    @GetMapping("/{playerId}")
    public PlayerResponse getPlayer(@PathVariable UUID playerId) {
        return playerService.getPlayer(playerId);
    }

    @GetMapping
    public List<PlayerResponse> listPlayers() {
        return playerService.listPlayers();
    }

    @GetMapping("/{playerId}/stats")
    public CareerStatsResponse getCareerStats(@PathVariable UUID playerId) {
        return playerService.getCareerStats(playerId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SCORER')")
    @PatchMapping("/{playerId}/stats")
    public CareerStatsResponse updateCareerStats(@PathVariable UUID playerId,
                                                 @Valid @RequestBody CareerStatsUpdateRequest request) {
        return playerService.updateCareerStats(playerId, request);
    }

    @GetMapping("/leaderboard/runs")
    public List<LeaderboardEntry> topByRuns(@RequestParam(defaultValue = "10") int limit) {
        return playerService.topByRuns(limit);
    }

    @GetMapping("/leaderboard/wickets")
    public List<LeaderboardEntry> topByWickets(@RequestParam(defaultValue = "10") int limit) {
        return playerService.topByWickets(limit);
    }
}