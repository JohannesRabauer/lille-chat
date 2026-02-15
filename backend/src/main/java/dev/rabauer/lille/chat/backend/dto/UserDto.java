package dev.rabauer.lille.chat.backend.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String displayName,
        String email
) {
}
