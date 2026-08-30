package com.cricket.user.dto;

import com.cricket.user.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String username,
        Role role,
        Instant expiresAt
) {
}