package com.cricket.stats.service;

import com.cricket.stats.entity.LiveMatchState;
import com.cricket.stats.event.BallBowledEvent;
import com.cricket.stats.repository.LiveMatchStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveMatchStateServiceTest {

    @Mock
    private LiveMatchStateRepository liveMatchStateRepository;

    @InjectMocks
    private LiveMatchStateService liveMatchStateService;

    @Test
    void applyBallEvent_createsNewStateOnFirstBallOfInnings() {
        UUID matchId = UUID.randomUUID();
        when(liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, 1)).thenReturn(Optional.empty());
        when(liveMatchStateRepository.save(any(LiveMatchState.class))).thenAnswer(inv -> inv.getArgument(0));

        BallBowledEvent event = legalBall(matchId, 4, false);

        LiveMatchState result = liveMatchStateService.applyBallEvent(event);

        assertThat(result.getTotalRuns()).isEqualTo(4);
        assertThat(result.getLegalBalls()).isEqualTo(1);
        assertThat(result.getWicketsLost()).isZero();
    }

    @Test
    void applyBallEvent_wideDoesNotIncrementLegalBallCount() {
        UUID matchId = UUID.randomUUID();
        LiveMatchState existing = LiveMatchState.builder()
                .matchId(matchId).inningsNumber(1).totalRuns(10).legalBalls(6).build();
        when(liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, 1)).thenReturn(Optional.of(existing));
        when(liveMatchStateRepository.save(any(LiveMatchState.class))).thenAnswer(inv -> inv.getArgument(0));

        BallBowledEvent wideBall = new BallBowledEvent(
                UUID.randomUUID(), matchId, 1, 1, 7,
                UUID.randomUUID(), "Batter", 0, 10, 10,
                UUID.randomUUID(), "Bowler", false, 0,
                true, false, 1, Instant.now());

        LiveMatchState result = liveMatchStateService.applyBallEvent(wideBall);

        assertThat(result.getLegalBalls()).isEqualTo(6); // unchanged — wides don't count
        assertThat(result.getTotalRuns()).isEqualTo(11); // but the extra run does count
    }

    @Test
    void applyBallEvent_wicketIncrementsWicketsLost() {
        UUID matchId = UUID.randomUUID();
        when(liveMatchStateRepository.findByMatchIdAndInningsNumber(matchId, 1)).thenReturn(Optional.empty());
        ArgumentCaptor<LiveMatchState> captor = ArgumentCaptor.forClass(LiveMatchState.class);
        when(liveMatchStateRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        BallBowledEvent wicketBall = legalBall(matchId, 0, true);

        liveMatchStateService.applyBallEvent(wicketBall);

        assertThat(captor.getValue().getWicketsLost()).isEqualTo(1);
    }

    private BallBowledEvent legalBall(UUID matchId, int runs, boolean wicket) {
        return new BallBowledEvent(
                UUID.randomUUID(), matchId, 1, 1, 1,
                UUID.randomUUID(), "Batter", runs, 0, runs,
                UUID.randomUUID(), "Bowler", wicket, wicket ? 1 : 0,
                false, false, 0, Instant.now());
    }
}
