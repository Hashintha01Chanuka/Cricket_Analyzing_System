package com.cricket.match.service;

import com.cricket.match.dto.BallRequest;
import com.cricket.match.dto.MatchRequest;
import com.cricket.match.dto.MatchResponse;
import com.cricket.match.dto.RunRateResponse;
import com.cricket.match.entity.Ball;
import com.cricket.match.entity.Match;
import com.cricket.match.entity.MatchFormat;
import com.cricket.match.event.BallBowledEvent;
import com.cricket.match.exception.MatchNotFoundException;
import com.cricket.match.kafka.MatchEventProducer;
import com.cricket.match.repository.BallRepository;
import com.cricket.match.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private BallRepository ballRepository;

    @Mock
    private MatchEventProducer matchEventProducer;

    @InjectMocks
    private MatchService matchService;

    private UUID matchId;
    private Match match;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        match = Match.builder()
                .id(matchId)
                .team1("India")
                .team2("Australia")
                .matchDate(LocalDateTime.now())
                .format(MatchFormat.ODI)
                .build();
    }

    @Test
    void createMatch_savesAndReturnsMatch() {
        MatchRequest request = new MatchRequest("India", "Australia", "MCG",
                LocalDateTime.now(), MatchFormat.ODI);
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        MatchResponse response = matchService.createMatch(request);

        assertThat(response.team1()).isEqualTo("India");
        assertThat(response.team2()).isEqualTo("Australia");
        assertThat(response.status()).isEqualTo(match.getStatus());
    }

    @Test
    void getMatch_throwsWhenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(matchRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatch(unknownId))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void currentRunRate_returnsZeroWhenNoBallsBowled() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(ballRepository.findByMatchAndInningsOrdered(matchId, 1)).thenReturn(List.of());

        RunRateResponse response = matchService.currentRunRate(matchId, 1, 6);

        assertThat(response.runRate()).isEqualTo(0.0);
        assertThat(response.ballsConsidered()).isZero();
    }

    @Test
    void currentRunRate_computesOverSlidingWindow() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        // 12 balls across overs 0 and 1: 6 runs/over in over 0, 12 runs/over in over 1.
        // With a 1-over window, only over 1's 12 runs off 6 balls should count -> run rate 12.0
        List<Ball> balls = List.of(
                ballAt(0, 1, 1), ballAt(0, 2, 1), ballAt(0, 3, 1),
                ballAt(0, 4, 1), ballAt(0, 5, 1), ballAt(0, 6, 1),
                ballAt(1, 1, 2), ballAt(1, 2, 2), ballAt(1, 3, 2),
                ballAt(1, 4, 2), ballAt(1, 5, 2), ballAt(1, 6, 2)
        );
        when(ballRepository.findByMatchAndInningsOrdered(matchId, 1)).thenReturn(balls);

        RunRateResponse response = matchService.currentRunRate(matchId, 1, 1);

        assertThat(response.ballsConsidered()).isEqualTo(6);
        assertThat(response.runsInWindow()).isEqualTo(12);
        assertThat(response.runRate()).isEqualTo(12.0);
    }

    private Ball ballAt(int over, int ballNumber, int runs) {
        return Ball.builder()
                .overNumber(over)
                .ballNumber(ballNumber)
                .runs(runs)
                .bowlerId(UUID.randomUUID())
                .batsmanId(UUID.randomUUID())
                .build();
    }

    @Test
    void addBall_publishesEventWithCorrectRunningTotal() {
        UUID batsmanId = UUID.randomUUID();
        UUID bowlerId = UUID.randomUUID();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        // Batter already has 22 runs from two prior balls this innings.
        when(ballRepository.findByMatchAndInningsAndBatsmanOrdered(matchId, 1, batsmanId))
                .thenReturn(List.of(ballWithRuns(batsmanId, 10), ballWithRuns(batsmanId, 12)));
        when(ballRepository.findByMatchAndInningsAndBowlerOrdered(matchId, 1, bowlerId))
                .thenReturn(List.of());

        BallRequest request = new BallRequest(1, 5, 3, bowlerId, "Bumrah", batsmanId, "Kohli",
                4, false, false, false, 0);

        matchService.addBall(matchId, request);

        ArgumentCaptor<BallBowledEvent> captor = ArgumentCaptor.forClass(BallBowledEvent.class);
        verify(matchEventProducer).publishBallBowled(captor.capture());

        BallBowledEvent event = captor.getValue();
        assertThat(event.batsmanPreviousRuns()).isEqualTo(22);
        assertThat(event.batsmanNewRuns()).isEqualTo(26); // 22 + 4 this ball
        assertThat(event.bowlerConsecutiveWickets()).isZero();
    }

    @Test
    void addBall_incrementsConsecutiveWicketStreakOnWicketBall() {
        UUID batsmanId = UUID.randomUUID();
        UUID bowlerId = UUID.randomUUID();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        when(ballRepository.findByMatchAndInningsAndBatsmanOrdered(matchId, 1, batsmanId))
                .thenReturn(List.of());
        // Bowler already has 2 trailing wickets this innings (a streak in progress).
        when(ballRepository.findByMatchAndInningsAndBowlerOrdered(matchId, 1, bowlerId))
                .thenReturn(List.of(wicketBall(bowlerId), wicketBall(bowlerId)));

        BallRequest request = new BallRequest(1, 8, 1, bowlerId, "Bumrah", batsmanId, "New Batter",
                0, true, false, false, 0);

        matchService.addBall(matchId, request);

        ArgumentCaptor<BallBowledEvent> captor = ArgumentCaptor.forClass(BallBowledEvent.class);
        verify(matchEventProducer).publishBallBowled(captor.capture());

        assertThat(captor.getValue().bowlerConsecutiveWickets()).isEqualTo(3); // hat-trick ball
    }

    @Test
    void addBall_resetsConsecutiveWicketStreakWhenNoWicketThisBall() {
        UUID batsmanId = UUID.randomUUID();
        UUID bowlerId = UUID.randomUUID();

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        when(ballRepository.findByMatchAndInningsAndBatsmanOrdered(matchId, 1, batsmanId))
                .thenReturn(List.of());
        when(ballRepository.findByMatchAndInningsAndBowlerOrdered(matchId, 1, bowlerId))
                .thenReturn(List.of(wicketBall(bowlerId), wicketBall(bowlerId)));

        BallRequest request = new BallRequest(1, 8, 1, bowlerId, "Bumrah", batsmanId, "New Batter",
                4, false, false, false, 0);

        matchService.addBall(matchId, request);

        ArgumentCaptor<BallBowledEvent> captor = ArgumentCaptor.forClass(BallBowledEvent.class);
        verify(matchEventProducer).publishBallBowled(captor.capture());

        assertThat(captor.getValue().bowlerConsecutiveWickets()).isZero();
    }

    private Ball ballWithRuns(UUID batsmanId, int runs) {
        return Ball.builder().batsmanId(batsmanId).runs(runs).build();
    }

    private Ball wicketBall(UUID bowlerId) {
        return Ball.builder().bowlerId(bowlerId).wicket(true).build();
    }
}
