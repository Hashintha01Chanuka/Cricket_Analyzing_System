package com.cricket.stats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record MatchResultRequest(
        UUID matchId,
        @NotBlank(message = "team1 is required") String team1,
        @NotBlank(message = "team2 is required") String team2,
        @NotBlank(message = "winner is required") String winner,
        String margin,
        @NotNull(message = "matchDate is required") LocalDate matchDate
) {
}
