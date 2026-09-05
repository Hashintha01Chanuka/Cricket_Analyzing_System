package com.cricket.notification.service.rule;

import com.cricket.notification.dto.MilestoneCheckRequest;
import com.cricket.notification.entity.Notification;
import com.cricket.notification.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CenturyRule implements MilestoneRule {

    private static final int THRESHOLD = 100;

    @Override
    public Optional<Notification> evaluate(MilestoneCheckRequest request) {
        boolean crossedCentury = request.previousRuns() < THRESHOLD && request.newRuns() >= THRESHOLD;

        if (!crossedCentury) {
            return Optional.empty();
        }

        String name = request.playerName() != null ? request.playerName() : request.playerId().toString();
        String message = name + " brings up a century! (" + request.newRuns() + " runs)";

        return Optional.of(Notification.builder()
                .type(NotificationType.CENTURY)
                .matchId(request.matchId())
                .playerId(request.playerId())
                .message(message)
                .build());
    }
}
