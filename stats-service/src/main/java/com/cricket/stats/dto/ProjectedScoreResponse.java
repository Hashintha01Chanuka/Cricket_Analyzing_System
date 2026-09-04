package com.cricket.stats.dto;

import java.util.UUID;

public record ProjectedScoreResponse(
        UUID matchId,
        double currentRunRate,
        double oversRemaining,
        int currentScore,
        int projectedFinalScore
) {
}
