package com.cricket.notification.kafka;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.event.BallBowledEvent;
import com.cricket.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * This is what removes the "no memory between calls" limitation noted in
 * MilestoneCheckRequest's javadoc: Match Service now computes the batter's
 * before/after totals and the bowler's consecutive-wicket streak on every
 * ball, so this consumer just translates the event into the same request
 * shape the REST endpoint already accepted and reuses the exact same rule
 * evaluation logic. The POST /check-milestone endpoint stays in place for
 * manual testing — this listener is simply a second, automatic caller of
 * the same service method.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BallEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "ball-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onBallBowled(BallBowledEvent event) {
        log.debug("Received ball event for match {} (over {}.{})",
                event.matchId(), event.overNumber(), event.ballNumber());

        MilestoneCheckRequest request = new MilestoneCheckRequest(
                event.matchId(),
                event.batsmanId(),
                event.batsmanName(),
                event.batsmanPreviousRuns(),
                event.batsmanNewRuns(),
                event.wicket(),
                event.bowlerId(),
                event.bowlerName(),
                event.bowlerConsecutiveWickets()
        );

        var fired = notificationService.checkMilestones(request);
        if (!fired.isEmpty()) {
            log.info("Milestone(s) fired for match {}: {}", event.matchId(), fired.size());
        }
    }
}