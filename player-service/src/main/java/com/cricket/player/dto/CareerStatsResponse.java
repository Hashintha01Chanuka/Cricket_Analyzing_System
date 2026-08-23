package com.cricket.player.dto;

import com.cricket.player.entity.CareerStats;

import java.util.UUID;

public record CareerStatsResponse(
        UUID playerId,
        int matches,
        int innings,
        int runsScored,
        int highestScore,
        int centuries,
        int fifties,
        double battingAverage,
        double battingStrikeRate,
        int wicketsTaken,
        double bowlingAverage,
        double bowlingEconomy
) {
    public static CareerStatsResponse from(CareerStats stats) {
        return new CareerStatsResponse(
                stats.getPlayer().getId(),
                stats.getMatches(),
                stats.getInnings(),
                stats.getRunsScored(),
                stats.getHighestScore(),
                stats.getCenturies(),
                stats.getFifties(),
                stats.getBattingAverage(),
                stats.getBattingStrikeRate(),
                stats.getWicketsTaken(),
                stats.getBowlingAverage(),
                stats.getBowlingEconomy()
        );
    }
}
