package com.cricket.notification.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors Match Service's BallBowledEvent shape. Kept as this service's own
 * copy rather than a shared library — same reasoning as MatchRunRateResponse
 * in Stats Service: a shared jar would couple deploy cycles, and a field
 * mismatch here surfaces immediately and visibly as a deserialization issue
 * rather than silently, which is the safer failure mode for decoupled
 * services. Only the fields this service actually needs are declared; a
 * consumer doesn't have to mirror every field the producer sends.
 */
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