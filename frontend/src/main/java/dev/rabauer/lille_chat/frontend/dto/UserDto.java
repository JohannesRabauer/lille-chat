package dev.rabauer.lille_chat.frontend.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String displayName,
        String email
) {
}
