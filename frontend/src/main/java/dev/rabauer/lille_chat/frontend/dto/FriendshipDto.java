package dev.rabauer.lille_chat.frontend.dto;

import java.time.Instant;
import java.util.UUID;

public record FriendshipDto(
        UUID id,
        UserDto friend,
        FriendshipStatus status,
        Instant createdAt
) {
}
