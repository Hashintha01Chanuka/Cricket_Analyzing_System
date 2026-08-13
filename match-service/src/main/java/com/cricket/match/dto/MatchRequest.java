package com.cricket.match.dto;

import com.cricket.match.entity.MatchFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MatchRequest(
        @NotBlank(message = "team1 is required")
        String team1,

        @NotBlank(message = "team2 is required")
        String team2,

        String venue,

        @NotNull(message = "matchDate is required")
        LocalDateTime matchDate,

        @NotNull(message = "format is required")
        MatchFormat format
) {
}
