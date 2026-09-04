package com.cricket.stats.dto;

import com.cricket.stats.entity.MatchResult;

import java.time.LocalDate;
import java.util.UUID;

public record MatchResultResponse(
        UUID id,
        UUID matchId,
        String team1,
        String team2,
        String winner,
        String margin,
        LocalDate matchDate
) {
    public static MatchResultResponse from(MatchResult result) {
        return new MatchResultResponse(result.getId(), result.getMatchId(), result.getTeam1(),
                result.getTeam2(), result.getWinner(), result.getMargin(), result.getMatchDate());
    }
}
