package com.cricket.match.controller;

import com.cricket.match.dto.BallRequest;
import com.cricket.match.dto.MatchRequest;
import com.cricket.match.dto.MatchResponse;
import com.cricket.match.dto.RunRateResponse;
import com.cricket.match.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(request));
    }

    @GetMapping("/{matchId}")
    public MatchResponse getMatch(@PathVariable UUID matchId) {
        return matchService.getMatch(matchId);
    }

    @GetMapping
    public List<MatchResponse> listMatches() {
        return matchService.listMatches();
    }

    @PostMapping("/{matchId}/balls")
    public ResponseEntity<MatchResponse> addBall(@PathVariable UUID matchId,@Valid @RequestBody BallRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.addBall(matchId, request));
    }

    @GetMapping("/{matchId}/run-rate")
    public RunRateResponse currentRunRate(@PathVariable UUID matchId, @RequestParam(defaultValue = "1") int innings,
                                          @RequestParam(defaultValue = "6") int overs) {
        return matchService.currentRunRate(matchId, innings, overs);
    }
}
