package dev.rabauer.lille_chat.frontend.dto;

import java.util.List;
import java.util.UUID;

public record CreateGroupConversationRequest(
        String name,
        List<UUID> participantIds
) {
}
