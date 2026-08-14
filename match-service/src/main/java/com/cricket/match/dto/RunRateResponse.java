package com.cricket.match.dto;

public record RunRateResponse(
        int oversWindow,
        int ballsConsidered,
        int runsInWindow,
        double runRate
) {
}
