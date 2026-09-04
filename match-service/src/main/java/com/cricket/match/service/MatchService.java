package com.cricket.match.service;

import com.cricket.match.dto.BallRequest;
import com.cricket.match.dto.MatchRequest;
import com.cricket.match.dto.MatchResponse;
import com.cricket.match.dto.RunRateResponse;
import com.cricket.match.entity.Ball;
import com.cricket.match.entity.Match;
import com.cricket.match.event.BallBowledEvent;
import com.cricket.match.exception.MatchNotFoundException;
import com.cricket.match.kafka.MatchEventProducer;
import com.cricket.match.repository.BallRepository;
import com.cricket.match.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final BallRepository ballRepository;
    private final MatchEventProducer matchEventProducer;

    @Transactional
    public MatchResponse createMatch(MatchRequest request) {
        Match match = Match.builder()
                .team1(request.team1())
                .team2(request.team2())
                .venue(request.venue())
                .matchDate(request.matchDate())
                .format(request.format())
                .build();

        return MatchResponse.from(matchRepository.save(match));
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatch(UUID matchId) {
        return MatchResponse.from(findMatchOrThrow(matchId));
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listMatches() {
        return matchRepository.findAll().stream().map(MatchResponse::from).toList();
    }

    @Transactional
    public MatchResponse addBall(UUID matchId, BallRequest request) {
        Match match = findMatchOrThrow(matchId);

        int batsmanPreviousRuns = sumRuns(matchId, request.inningsNumber(), request.batsmanId());
        int bowlerConsecutiveWicketsBefore = trailingConsecutiveWickets(matchId, request.inningsNumber(), request.bowlerId());

        Ball ball = Ball.builder()
                .inningsNumber(request.inningsNumber())
                .overNumber(request.overNumber())
                .ballNumber(request.ballNumber())
                .bowlerId(request.bowlerId())
                .batsmanId(request.batsmanId())
                .runs(request.runs())
                .wicket(request.wicket())
                .wide(request.wide())
                .noBall(request.noBall())
                .extras(request.extras())
                .build();

        match.addBall(ball);
        MatchResponse response = MatchResponse.from(matchRepository.save(match));

        int batsmanNewRuns = batsmanPreviousRuns + request.runs();
        int bowlerConsecutiveWicketsAfter = request.wicket()
                ? bowlerConsecutiveWicketsBefore + 1
                : 0;

        BallBowledEvent event = new BallBowledEvent(
                UUID.randomUUID(),
                matchId,
                request.inningsNumber(),
                request.overNumber(),
                request.ballNumber(),
                request.batsmanId(),
                request.batsmanName(),
                request.runs(),
                batsmanPreviousRuns,
                batsmanNewRuns,
                request.bowlerId(),
                request.bowlerName(),
                request.wicket(),
                bowlerConsecutiveWicketsAfter,
                request.wide(),
                request.noBall(),
                request.extras(),
                Instant.now()
        );
        matchEventProducer.publishBallBowled(event);

        return response;
    }

    /**
     * Computes the run rate over the last N overs using a sliding window.
     * We walk the innings' balls once (O(n)) and keep a running sum of runs,
     * evicting balls that fall outside the window from the front of the deque
     * as we go — this avoids re-summing the whole window on every ball, which
     * is the naive O(n * windowSize) approach.
     */
    @Transactional(readOnly = true)
    public RunRateResponse currentRunRate(UUID matchId, int inningsNumber, int oversWindow) {
        findMatchOrThrow(matchId);

        List<Ball> balls = ballRepository.findByMatchAndInningsOrdered(matchId, inningsNumber);
        if (balls.isEmpty()) {
            return new RunRateResponse(oversWindow, 0, 0, 0.0);
        }

        int latestOver = balls.get(balls.size() - 1).getOverNumber();
        int windowStartOver = Math.max(0, latestOver - oversWindow + 1);

        Deque<Ball> window = new ArrayDeque<>();
        int runsInWindow = 0;

        for (Ball ball : balls) {
            window.addLast(ball);
            runsInWindow += ball.getRuns() + ball.getExtras();

            while (!window.isEmpty() && window.peekFirst().getOverNumber() < windowStartOver) {
                Ball evicted = window.pollFirst();
                runsInWindow -= evicted.getRuns() + evicted.getExtras();
            }
        }

        int ballsConsidered = window.size();
        double oversFaced = ballsConsidered / 6.0;
        double runRate = oversFaced == 0 ? 0.0 : runsInWindow / oversFaced;

        return new RunRateResponse(oversWindow, ballsConsidered, runsInWindow, Math.round(runRate * 100.0) / 100.0);
    }

    /** Sum of runs the batter has scored so far this innings, before the current ball. */
    private int sumRuns(UUID matchId, int inningsNumber, UUID batsmanId) {
        return ballRepository.findByMatchAndInningsAndBatsmanOrdered(matchId, inningsNumber, batsmanId)
                .stream()
                .mapToInt(Ball::getRuns)
                .sum();
    }

    /**
     * Walks the bowler's deliveries backward from the most recent one and
     * counts how many trailing wickets are unbroken by a non-wicket ball —
     * i.e. the current active streak going into this delivery. A single
     * backward scan is enough since we only need the *trailing* run, not a
     * count of every streak in the innings.
     */
    private int trailingConsecutiveWickets(UUID matchId, int inningsNumber, UUID bowlerId) {
        List<Ball> bowlerBalls = ballRepository.findByMatchAndInningsAndBowlerOrdered(matchId, inningsNumber, bowlerId);

        int streak = 0;
        for (int i = bowlerBalls.size() - 1; i >= 0; i--) {
            if (bowlerBalls.get(i).isWicket()) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private Match findMatchOrThrow(UUID matchId) {
        return matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
    }
}