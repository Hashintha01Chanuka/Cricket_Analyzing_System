package com.cricket.player.dto;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * All fields are deltas from a single innings/match, not absolute totals.
 * The service adds these onto the player's existing running totals. This
 * mirrors how the data actually arrives in practice — one performance at a
 * time, driven by ball-by-ball events from Match Service — rather than the
 * caller having to know and resend the player's entire career total.
 */
public record CareerStatsUpdateRequest(
        @PositiveOrZero int runsScored,
        @PositiveOrZero int ballsFaced,
        boolean isOut,
        @PositiveOrZero int wicketsTaken,
        @PositiveOrZero int runsConceded,
        @PositiveOrZero int ballsBowled
) {
}
