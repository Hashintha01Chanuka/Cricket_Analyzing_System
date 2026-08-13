package com.cricket.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record BallRequest(
        @NotNull int inningsNumber,
        @Min(0) int overNumber,
        @Min(1) int ballNumber,
        @NotNull(message = "bowlerId is required") UUID bowlerId,
        @NotNull(message = "batsmanId is required") UUID batsmanId,
        @PositiveOrZero int runs,
        boolean wicket,
        boolean wide,
        boolean noBall,
        @PositiveOrZero int extras
) {
}
