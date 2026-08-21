package com.cricket.player.dto;

import com.cricket.player.entity.PlayerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PlayerRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "country is required") String country,
        @NotNull(message = "role is required") PlayerRole role,
        String battingStyle,
        String bowlingStyle,
        @Past(message = "dateOfBirth must be in the past") LocalDate dateOfBirth
) {
}
