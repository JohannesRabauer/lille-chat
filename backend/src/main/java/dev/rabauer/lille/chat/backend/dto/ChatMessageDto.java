package dev.rabauer.lille.chat.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        UUID conversationId,
        UserDto sender,
        String content,
        Instant sentAt
) {
}
