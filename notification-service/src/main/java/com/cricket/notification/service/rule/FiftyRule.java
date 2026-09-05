package com.cricket.notification.service.rule;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.entity.Notification;
import com.cricket.notification.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FiftyRule implements MilestoneRule {

    private static final int THRESHOLD = 50;
    private static final int CENTURY = 100;

    @Override
    public Optional<Notification> evaluate(MilestoneCheckRequest request) {
        // Only fires exactly on the ball that crosses 50 — not every ball
        // after — and explicitly not for the ball that crosses 100 (that's
        // CenturyRule's job), so a batter doesn't get both notifications
        // firing off the same delivery when they go from, say, 49 to 101
        // in one very unlikely six.
        boolean crossedFifty = request.previousRuns() < THRESHOLD && request.newRuns() >= THRESHOLD;
        boolean alsoCrossedCentury = request.newRuns() >= CENTURY;

        if (!crossedFifty || alsoCrossedCentury) {
            return Optional.empty();
        }

        String name = request.playerName() != null ? request.playerName() : request.playerId().toString();
        String message = name + " brings up a fifty! (" + request.newRuns() + " runs)";

        return Optional.of(Notification.builder()
                .type(NotificationType.FIFTY)
                .matchId(request.matchId())
                .playerId(request.playerId())
                .message(message)
                .build());
    }
}
