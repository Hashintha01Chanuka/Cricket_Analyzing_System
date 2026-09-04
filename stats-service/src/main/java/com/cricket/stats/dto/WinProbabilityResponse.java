package com.cricket.stats.dto;

public record WinProbabilityResponse(
        double battingTeamWinProbability,
        double requiredRunRate,
        int runsNeeded,
        String note
) {
}
