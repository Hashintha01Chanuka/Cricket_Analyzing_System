package com.cricket.stats.service;

import com.cricket.stats.client.MatchServiceClient;
import com.cricket.stats.dto.*;
import com.cricket.stats.entity.LiveMatchState;
import com.cricket.stats.entity.MatchResult;
import com.cricket.stats.exception.LiveStateNotAvailableException;
import com.cricket.stats.repository.LiveMatchStateRepository;
import com.cricket.stats.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final MatchResultRepository matchResultRepository;
    private final MatchServiceClient matchServiceClient;
    private final LiveMatchStateRepository liveMatchStateRepository;

    @Transactional
    public MatchResultResponse recordResult(MatchResultRequest request) {
        MatchResult result = MatchResult.builder()
                .matchId(request.matchId())
                .team1(request.team1())
                .team2(request.team2())
                .winner(request.winner())
                .margin(request.margin())
                .matchDate(request.matchDate())
                .build();

        return MatchResultResponse.from(matchResultRepository.save(result));
    }

    /**
     * Counts wins per team with a single pass and a HashMap<String,Integer>
     * tally — O(n) for n historical matches between the two teams, rather
     * than filtering the list twice (once per team).
     */
    @Transactional(readOnly = true)
    public HeadToHeadResponse headToHead(String team1, String team2) {
        List<MatchResult> matches = matchResultRepository.findHeadToHead(team1, team2);

        Map<String, Integer> winTally = new HashMap<>();
        for (MatchResult match : matches) {
            winTally.merge(match.getWinner(), 1, Integer::sum);
        }

        return new HeadToHeadResponse(
                team1,
                team2,
                winTally.getOrDefault(team1, 0),
                winTally.getOrDefault(team2, 0),
                matches.size(),
                matches.stream().map(MatchResultResponse::from).toList()
        );
    }

    /**
     * Heuristic, not machine learning — deliberately simple and explainable
     * rather than trained on historical data. Modeled as a logistic curve
     * centered on an "achievable" run rate that scales with wickets in
     * hand: the more wickets remaining, the higher a required run rate the
     * batting side can realistically still chase down. Worth being upfront
     * about this being a heuristic if asked — a real win-probability model
     * (like the ones broadcasters use) is trained on ball-by-ball outcomes
     * across thousands of matches, which is out of scope here.
     */
    public WinProbabilityResponse calculateWinProbability(WinProbabilityRequest request) {
        int runsNeeded = Math.max(request.target() - request.currentScore(), 0);

        if (runsNeeded == 0) {
            return new WinProbabilityResponse(1.0, 0.0, 0, "Target already reached");
        }
        if (request.oversRemaining() <= 0) {
            return new WinProbabilityResponse(0.0, Double.POSITIVE_INFINITY, runsNeeded, "No overs remaining");
        }

        double requiredRunRate = runsNeeded / request.oversRemaining();
        int wicketsInHand = 10 - request.wicketsLost();

        double achievableRunRate = 6.0 + (wicketsInHand * 0.6); // caps near 12 with all wickets in hand
        double probability = 1.0 / (1.0 + Math.exp((requiredRunRate - achievableRunRate) * 0.5));

        return new WinProbabilityResponse(
                round(probability),
                round(requiredRunRate),
                runsNeeded,
                "Heuristic estimate based on required run rate vs. wickets in hand"
        );
    }

    /**
     * Calls Match Service synchronously to get the current run rate, then
     * extrapolates a projected final score. This is the one place in the
     * MVP where two services talk to each other live over REST — see
     * MatchServiceClient for why this is a temporary design.
     */
    public ProjectedScoreResponse projectFinalScore(UUID matchId, int innings, int oversWindow,
                                                    int currentScore, double oversRemaining) {
        MatchRunRateResponse runRate = matchServiceClient.getCurrentRunRate(matchId, innings, oversWindow);
        int projected = currentScore + (int) Math.round(runRate.runRate() * oversRemaining);

        return new ProjectedScoreResponse(matchId, runRate.runRate(), oversRemaining, currentScore, projected);
    }

    /**
     * Reads the Kafka-derived live state directly — no call to Match
     * Service at all. This is the replacement for the synchronous REST
     * approach below: it's faster (a local DB read vs. a network hop to
     * another service), and it keeps working even if Match Service is
     * temporarily down, since Stats Service already has everything it
     * needs from the events it already consumed.
     */
    @Transactional(readOnly = true)
    public LiveMatchStateResponse getLiveState(UUID matchId, int innings) {
        LiveMatchState state = liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, innings)
                .orElseThrow(() -> new LiveStateNotAvailableException(matchId, innings));

        return new LiveMatchStateResponse(
                matchId, innings, state.getTotalRuns(), state.getWicketsLost(),
                state.getOversBowled(), state.getCurrentRunRate()
        );
    }

    /**
     * Kafka-driven equivalent of projectFinalScore() below — same
     * extrapolation math, but sourced from locally consumed events instead
     * of a live REST call to Match Service. Kept side by side with the
     * REST version deliberately, as a direct before/after comparison: this
     * is the one piece of the MVP where you can point at two working
     * implementations of the same feature and explain why the event-driven
     * one is the better fit for production.
     */
    @Transactional(readOnly = true)
    public ProjectedScoreResponse projectFinalScoreFromLiveState(UUID matchId, int innings, double oversRemaining) {
        LiveMatchState state = liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, innings)
                .orElseThrow(() -> new LiveStateNotAvailableException(matchId, innings));

        double runRate = state.getCurrentRunRate();
        int projected = state.getTotalRuns() + (int) Math.round(runRate * oversRemaining);

        return new ProjectedScoreResponse(matchId, runRate, oversRemaining, state.getTotalRuns(), projected);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}