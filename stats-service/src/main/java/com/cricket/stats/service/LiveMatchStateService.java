package com.cricket.stats.service;

import com.cricket.stats.entity.LiveMatchState;
import com.cricket.stats.event.BallBowledEvent;
import com.cricket.stats.repository.LiveMatchStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Deliberately separate from BallEventConsumer (the @KafkaListener class)
 * so the actual update logic — the part worth unit testing — doesn't
 * require a Kafka broker or Spring context to test. The listener is a thin
 * adapter that just calls this.
 */
@Service
@RequiredArgsConstructor
public class LiveMatchStateService {

    private final LiveMatchStateRepository liveMatchStateRepository;

    @Transactional
    public LiveMatchState applyBallEvent(BallBowledEvent event) {
        LiveMatchState state = liveMatchStateRepository
                .findByMatchIdAndInningsNumber(event.matchId(), event.inningsNumber())
                .orElseGet(() -> LiveMatchState.builder()
                        .matchId(event.matchId())
                        .inningsNumber(event.inningsNumber())
                        .build());

        state.setTotalRuns(state.getTotalRuns() + event.runsThisBall() + event.extras());

        // Wides and no-balls don't count as legal deliveries against the over.
        if (!event.wide() && !event.noBall()) {
            state.setLegalBalls(state.getLegalBalls() + 1);
        }

        if (event.wicket()) {
            state.setWicketsLost(state.getWicketsLost() + 1);
        }

        state.setUpdatedAt(LocalDateTime.now());
        return liveMatchStateRepository.save(state);
    }
}