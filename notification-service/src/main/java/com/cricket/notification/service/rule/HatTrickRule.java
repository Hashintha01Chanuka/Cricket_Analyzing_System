package com.cricket.notification.service.rule;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.entity.Notification;
import com.cricket.notification.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * consecutiveWicketsForBowler is tracked and passed in by the caller (see
 * MilestoneCheckRequest's javadoc) rather than by this service, which is a
 * known MVP simplification — Notification Service has no memory of prior
 * balls in this design. Once this becomes a Kafka consumer, tracking that
 * streak internally (keyed by bowlerId + matchId + innings) becomes natural
 * because the service will be seeing every ball in order anyway.
 */
@Component
public class HatTrickRule implements MilestoneRule {

    private static final int THRESHOLD = 3;

    @Override
    public Optional<Notification> evaluate(MilestoneCheckRequest request) {
        if (!request.wicketTakenThisBall() || request.consecutiveWicketsForBowler() != THRESHOLD) {
            return Optional.empty();
        }

        String name = request.bowlerName() != null ? request.bowlerName() : request.bowlerId().toString();
        String message = name + " has taken a hat-trick!";

        return Optional.of(Notification.builder()
                .type(NotificationType.HAT_TRICK)
                .matchId(request.matchId())
                .playerId(request.bowlerId())
                .message(message)
                .build());
    }
}
