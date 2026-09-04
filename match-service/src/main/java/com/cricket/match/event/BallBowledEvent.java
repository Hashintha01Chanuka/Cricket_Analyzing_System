package com.cricket.match.event;

import java.time.Instant;
import java.util.UUID;

public record BallBowledEvent(
        UUID eventId,
        UUID matchId,
        int inningsNumber,
        int overNumber,
        int ballNumber,

        UUID batsmanId,
        String batsmanName,
        int runsThisBall,
        int batsmanPreviousRuns,
        int batsmanNewRuns,

        UUID bowlerId,
        String bowlerName,
        boolean wicket,
        int bowlerConsecutiveWickets,

        boolean wide,
        boolean noBall,
        int extras,

        Instant timestamp
) {
}