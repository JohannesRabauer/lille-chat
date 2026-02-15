package dev.rabauer.lille_chat.frontend.dto;

public record SseEventDto(
        String type,
        Object payload
) {
}
