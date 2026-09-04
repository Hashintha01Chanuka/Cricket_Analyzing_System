package com.cricket.stats.dto;

import java.util.UUID;

public record LiveMatchStateResponse(
        UUID matchId,
        int inningsNumber,
        int totalRuns,
        int wicketsLost,
        double oversBowled,
        double currentRunRate
) {
}