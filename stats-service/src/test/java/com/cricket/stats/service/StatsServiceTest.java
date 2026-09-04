package com.cricket.stats.service;

import com.cricket.stats.client.MatchServiceClient;
import com.cricket.stats.dto.HeadToHeadResponse;
import com.cricket.stats.dto.LiveMatchStateResponse;
import com.cricket.stats.dto.MatchRunRateResponse;
import com.cricket.stats.dto.ProjectedScoreResponse;
import com.cricket.stats.dto.WinProbabilityRequest;
import com.cricket.stats.dto.WinProbabilityResponse;
import com.cricket.stats.entity.LiveMatchState;
import com.cricket.stats.entity.MatchResult;
import com.cricket.stats.exception.LiveStateNotAvailableException;
import com.cricket.stats.repository.LiveMatchStateRepository;
import com.cricket.stats.repository.MatchResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private MatchServiceClient matchServiceClient;

    @Mock
    private LiveMatchStateRepository liveMatchStateRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void headToHead_tallysWinsPerTeamAcrossMixedOrderMatches() {
        List<MatchResult> history = List.of(
                MatchResult.builder().team1("India").team2("Australia").winner("India")
                        .matchDate(LocalDate.of(2024, 1, 1)).build(),
                MatchResult.builder().team1("Australia").team2("India").winner("Australia")
                        .matchDate(LocalDate.of(2024, 2, 1)).build(),
                MatchResult.builder().team1("India").team2("Australia").winner("India")
                        .matchDate(LocalDate.of(2024, 3, 1)).build()
        );
        when(matchResultRepository.findHeadToHead("India", "Australia")).thenReturn(history);

        HeadToHeadResponse response = statsService.headToHead("India", "Australia");

        assertThat(response.team1Wins()).isEqualTo(2);
        assertThat(response.team2Wins()).isEqualTo(1);
        assertThat(response.totalMatches()).isEqualTo(3);
    }

    @Test
    void winProbability_returnsFullConfidenceWhenTargetAlreadyReached() {
        WinProbabilityRequest request = new WinProbabilityRequest(150, 150, 3, 5.0, 20.0);

        WinProbabilityResponse response = statsService.calculateWinProbability(request);

        assertThat(response.battingTeamWinProbability()).isEqualTo(1.0);
        assertThat(response.runsNeeded()).isZero();
    }

    @Test
    void winProbability_returnsZeroWhenNoOversRemainAndRunsStillNeeded() {
        WinProbabilityRequest request = new WinProbabilityRequest(150, 120, 3, 0.0, 20.0);

        WinProbabilityResponse response = statsService.calculateWinProbability(request);

        assertThat(response.battingTeamWinProbability()).isZero();
    }

    @Test
    void winProbability_higherWithMoreWicketsInHandForSameChase() {
        WinProbabilityRequest fewWicketsLost = new WinProbabilityRequest(180, 100, 2, 5.0, 20.0);
        WinProbabilityRequest manyWicketsLost = new WinProbabilityRequest(180, 100, 8, 5.0, 20.0);

        double probFewLost = statsService.calculateWinProbability(fewWicketsLost).battingTeamWinProbability();
        double probManyLost = statsService.calculateWinProbability(manyWicketsLost).battingTeamWinProbability();

        assertThat(probFewLost).isGreaterThan(probManyLost);
    }

    @Test
    void projectFinalScore_extrapolatesFromCurrentRunRate() {
        UUID matchId = UUID.randomUUID();
        when(matchServiceClient.getCurrentRunRate(matchId, 1, 6))
                .thenReturn(new MatchRunRateResponse(6, 36, 60, 10.0));

        ProjectedScoreResponse response = statsService.projectFinalScore(matchId, 1, 6, 120, 5.0);

        assertThat(response.projectedFinalScore()).isEqualTo(170); // 120 + (10.0 * 5)
    }

    @Test
    void getLiveState_throwsWhenNoEventsConsumedYet() {
        UUID matchId = UUID.randomUUID();
        when(liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statsService.getLiveState(matchId, 1))
                .isInstanceOf(LiveStateNotAvailableException.class);
    }

    @Test
    void getLiveState_returnsCurrentScoreAndRunRateFromConsumedEvents() {
        UUID matchId = UUID.randomUUID();
        LiveMatchState state = LiveMatchState.builder()
                .matchId(matchId).inningsNumber(1)
                .totalRuns(90).legalBalls(60).wicketsLost(3)
                .build();
        when(liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, 1)).thenReturn(Optional.of(state));

        LiveMatchStateResponse response = statsService.getLiveState(matchId, 1);

        assertThat(response.totalRuns()).isEqualTo(90);
        assertThat(response.wicketsLost()).isEqualTo(3);
        assertThat(response.oversBowled()).isEqualTo(10.0); // 60 balls / 6
        assertThat(response.currentRunRate()).isEqualTo(9.0); // 90 / 10 overs
    }

    @Test
    void projectFinalScoreFromLiveState_extrapolatesWithoutCallingMatchService() {
        UUID matchId = UUID.randomUUID();
        LiveMatchState state = LiveMatchState.builder()
                .matchId(matchId).inningsNumber(1)
                .totalRuns(100).legalBalls(60).wicketsLost(2)
                .build();
        when(liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, 1)).thenReturn(Optional.of(state));

        ProjectedScoreResponse response = statsService.projectFinalScoreFromLiveState(matchId, 1, 10.0);

        // current run rate = 100/10 overs = 10.0; projected = 100 + 10.0*10 = 200
        assertThat(response.projectedFinalScore()).isEqualTo(200);
    }
}
