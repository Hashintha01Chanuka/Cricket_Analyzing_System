package com.cricket.stats.controller;

import com.cricket.stats.dto.*;
import com.cricket.stats.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** ADMIN only — recording a completed match's result is an administrative action. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/results")
    public ResponseEntity<MatchResultResponse> recordResult(@Valid @RequestBody MatchResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statsService.recordResult(request));
    }

    @GetMapping("/head-to-head")
    public HeadToHeadResponse headToHead(@RequestParam String team1, @RequestParam String team2) {
        return statsService.headToHead(team1, team2);
    }

    @PostMapping("/win-probability")
    public WinProbabilityResponse winProbability(@Valid @RequestBody WinProbabilityRequest request) {
        return statsService.calculateWinProbability(request);
    }

    @GetMapping("/matches/{matchId}/projected-score")
    public ProjectedScoreResponse projectedScore(@PathVariable UUID matchId,
                                                 @RequestParam(defaultValue = "1") int innings,
                                                 @RequestParam(defaultValue = "6") int oversWindow,
                                                 @RequestParam int currentScore,
                                                 @RequestParam double oversRemaining) {
        return statsService.projectFinalScore(matchId, innings, oversWindow, currentScore, oversRemaining);
    }

    /** Kafka-derived live state — no call to Match Service. See LiveMatchState's javadoc. */
    @GetMapping("/matches/{matchId}/live")
    public LiveMatchStateResponse liveState(@PathVariable UUID matchId,
                                            @RequestParam(defaultValue = "1") int innings) {
        return statsService.getLiveState(matchId, innings);
    }

    /** Kafka-driven equivalent of /projected-score — compare the two side by side. */
    @GetMapping("/matches/{matchId}/projected-score-live")
    public ProjectedScoreResponse projectedScoreFromLive(@PathVariable UUID matchId,
                                                         @RequestParam(defaultValue = "1") int innings,
                                                         @RequestParam double oversRemaining) {
        return statsService.projectFinalScoreFromLiveState(matchId, innings, oversRemaining);
    }
}