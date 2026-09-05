package com.cricket.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * Carries a before/after snapshot for one ball, not just the ball itself —
 * detecting "crossed 50" requires knowing the batter's score *before* this
 * ball (e.g. 48) and *after* it (e.g. 51), not just the runs off this single
 * delivery. In the current MVP, whatever calls this endpoint (a scorer via
 * Postman, or later Match Service) is responsible for tracking and passing
 * those running totals. Once Kafka is wired in (Phase 3), this service
 * would instead consume ball events directly and maintain that running
 * state itself — this request shape is designed to make that swap
 * straightforward, since the detection logic below wouldn't need to change,
 * only where the before/after numbers come from.
 */
public record MilestoneCheckRequest(
        @NotNull UUID matchId,
        @NotNull UUID playerId,
        String playerName,

        @PositiveOrZero int previousRuns,
        @PositiveOrZero int newRuns,

        boolean wicketTakenThisBall,
        UUID bowlerId,
        String bowlerName,
        @PositiveOrZero int consecutiveWicketsForBowler
) {
}
