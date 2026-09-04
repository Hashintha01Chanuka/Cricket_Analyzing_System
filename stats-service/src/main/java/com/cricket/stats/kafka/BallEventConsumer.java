package com.cricket.stats.kafka;

import com.cricket.stats.event.BallBowledEvent;
import com.cricket.stats.service.LiveMatchStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BallEventConsumer {

    private final LiveMatchStateService liveMatchStateService;

    @KafkaListener(topics = "ball-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onBallBowled(BallBowledEvent event) {
        var updated = liveMatchStateService.applyBallEvent(event);
        log.debug("Updated live state for match {} innings {}: {}/{} in {} overs",
                event.matchId(), event.inningsNumber(), updated.getTotalRuns(),
                updated.getWicketsLost(), updated.getOversBowled());
    }
}