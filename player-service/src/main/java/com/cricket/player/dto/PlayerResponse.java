package com.cricket.player.dto;

import com.cricket.player.entity.Player;
import com.cricket.player.entity.PlayerRole;

import java.time.LocalDate;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String name,
        String country,
        PlayerRole role,
        String battingStyle,
        String bowlingStyle,
        LocalDate dateOfBirth
) {
    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getCountry(),
                player.getRole(),
                player.getBattingStyle(),
                player.getBowlingStyle(),
                player.getDateOfBirth()
        );
    }
}
