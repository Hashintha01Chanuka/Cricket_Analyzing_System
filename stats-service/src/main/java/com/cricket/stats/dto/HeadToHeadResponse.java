package com.cricket.stats.dto;

import java.util.List;

public record HeadToHeadResponse(
        String team1,
        String team2,
        int team1Wins,
        int team2Wins,
        int totalMatches,
        List<MatchResultResponse> history
) {
}
