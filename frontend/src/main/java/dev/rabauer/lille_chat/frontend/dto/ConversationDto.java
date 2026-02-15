package dev.rabauer.lille_chat.frontend.dto;

import java.util.List;
import java.util.UUID;

public record ConversationDto(
        UUID id,
        String name,
        ConversationType type,
        List<UserDto> participants,
        ChatMessageDto lastMessage
) {
}
