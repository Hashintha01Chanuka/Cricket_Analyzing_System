package com.cricket.user.dto;

import com.cricket.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull(message = "role is required") Role role
) {
}