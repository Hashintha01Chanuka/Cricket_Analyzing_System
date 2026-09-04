package com.cricket.stats.dto;

/**
 * Mirrors Match Service's RunRateResponse shape. Stats Service keeps its own
 * copy of this DTO rather than sharing a jar with Match Service — a shared
 * DTO library would couple the two services' deploy cycles together, which
 * defeats the point of splitting them. A field-name mismatch here would
 * surface immediately as a deserialization/null-value bug, which is an
 * acceptable, visible cost for keeping the services decoupled.
 */
public record MatchRunRateResponse(
        int oversWindow,
        int ballsConsidered,
        int runsInWindow,
        double runRate
) {
}
