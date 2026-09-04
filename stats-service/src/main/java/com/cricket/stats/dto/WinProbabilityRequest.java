package com.cricket.stats.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public record WinProbabilityRequest(
        @PositiveOrZero int target,
        @PositiveOrZero int currentScore,
        @Min(0) @Max(10) int wicketsLost,
        @Min(0) double oversRemaining,
        @Min(1) double totalOvers
) {
}
