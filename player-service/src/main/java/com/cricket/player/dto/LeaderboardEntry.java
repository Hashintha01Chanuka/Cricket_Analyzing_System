package com.cricket.player.dto;

import java.util.UUID;

public record LeaderboardEntry(
        UUID playerId,
        String name,
        int value
) {
}
